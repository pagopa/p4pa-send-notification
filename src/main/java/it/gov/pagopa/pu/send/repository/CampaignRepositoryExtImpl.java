package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.config.BaseEntityListener;
import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.Campaign.Fields;
import it.gov.pagopa.pu.send.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class CampaignRepositoryExtImpl implements CampaignRepositoryExt {
  private final MongoTemplate mongoTemplate;

  public static final String FIELD_COUNTERS_TEMPLATE = "%s.%s";
  public static final String FIELD_COUNTERS_TOTAL = FIELD_COUNTERS_TEMPLATE.formatted(Fields.counters, Counters.Fields.total);
  public static final String FIELD_COUNTERS_FULL_RECALCULATION_DATE = FIELD_COUNTERS_TEMPLATE.formatted(Fields.counters, Counters.Fields.fullRecalculationDate);

  public CampaignRepositoryExtImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  private UpdateResult updateFirst(Query query, Update update) {
    return mongoTemplate.updateFirst(query, BaseEntityListener.setTechFieldsOnDocumentUpdate(update), Campaign.class);
  }

  @Override
  public UpdateResult incrementTotalAndUpdateEndDate(String campaignId, LocalDate endDate) {
    return updateFirst(
      Query.query(Criteria
        .where(Fields.campaignId).is(campaignId)
      ),
      new Update()
        .inc(FIELD_COUNTERS_TOTAL)
        .max(Fields.endDate, endDate)
    );
  }

  @Override
  public UpdateResult updateCampaignCounters(String campaignId, NotificationStatusChangeDTO notificationStatusChangeDTO) {
    Update update = new Update();
    if(notificationStatusChangeDTO.getIncrFields()!=null){
      notificationStatusChangeDTO.getIncrFields().forEach(f ->
        update.inc(FIELD_COUNTERS_TEMPLATE.formatted(Fields.counters, f), 1));
    }
    if(notificationStatusChangeDTO.getDecrFields()!=null) {
      notificationStatusChangeDTO.getDecrFields().forEach(f ->
        update.inc(FIELD_COUNTERS_TEMPLATE.formatted(Fields.counters, f), -1));
    }
    return updateFirst(
      Query.query(Criteria
        .where(Fields.campaignId).is(campaignId)
      ),
      update
    );
  }

  @Override
  public UpdateResult updateStartDate(String campaignId, LocalDate startDate) {
    return updateDate(campaignId, Fields.startDate, startDate);
  }

  @Override
  public UpdateResult updateEndDate(String campaignId, LocalDate endDate) {
    return updateDate(campaignId, Fields.endDate, endDate);
  }

  private UpdateResult updateDate(String campaignId, String field, LocalDate date) {
    return updateFirst(
      Query.query(Criteria.where(Fields.campaignId).is(campaignId)),
      new Update().set(field, date)
    );
  }

  @Override
  public Page<Campaign> findCampaignsByFilters(CampaignFiltersDTO campaignFiltersDTO, Pageable pageable) {
    Query query = new Query();
    query.addCriteria(Criteria
      .where(Fields.organizationId).is(campaignFiltersDTO.getOrganizationId()));
    if(campaignFiltersDTO.getDateFrom()!=null && campaignFiltersDTO.getDateTo()!=null){
      query.addCriteria(Criteria.where(Fields.startDate).lte(campaignFiltersDTO.getDateTo())
        .and(Fields.endDate).gte(campaignFiltersDTO.getDateFrom()));
    }
    if(!CollectionUtils.isEmpty(campaignFiltersDTO.getOrgSubUnitCodes())){
      if(Boolean.TRUE.equals(campaignFiltersDTO.getFetchAll())){
        query.addCriteria(new Criteria().orOperator(
          Criteria.where(Fields.orgSubUnitCode).in(campaignFiltersDTO.getOrgSubUnitCodes()),
          Criteria.where(Fields.orgSubUnitCode).isNull()
        ));
      }else {
        query.addCriteria(Criteria.where(Fields.orgSubUnitCode).in(campaignFiltersDTO.getOrgSubUnitCodes()));
      }
    }else{
      query.addCriteria(Criteria.where(Fields.orgSubUnitCode).isNull());
    }
    if(StringUtils.isNotBlank(campaignFiltersDTO.getCampaignName())){
      query.addCriteria(Criteria.where(Fields.campaignName).regex(Pattern.quote(campaignFiltersDTO.getCampaignName()), "i"));
    }
    if(StringUtils.isNotBlank(campaignFiltersDTO.getExternalCampaignId())){
      query.addCriteria(Criteria.where(Fields.externalId).is(campaignFiltersDTO.getExternalCampaignId()));
    }

    long count = mongoTemplate.count(query, Campaign.class);
    query.with(pageable);
    List<Campaign> campaigns = mongoTemplate.find(query, Campaign.class);
    return new PageImpl<>(campaigns, pageable, count);
  }

  @Override
  public UpdateResult updateCampaignName(String campaignId, String name) {
    return updateFirst(
      Query.query(Criteria.where(Fields.campaignId).is(campaignId)),
      new Update().set(Fields.campaignName, name)
    );
  }

  @Override
  public OffsetDateTime findLatestFullRecalculationDate() {
    Aggregation aggregation = Aggregation.newAggregation( // Did not aggregate with MAX because execution looks unstable on Cosmos
      Aggregation.sort(Sort.Direction.DESC, FIELD_COUNTERS_FULL_RECALCULATION_DATE),
      Aggregation.limit(1),
      Aggregation.project()
        .and(FIELD_COUNTERS_FULL_RECALCULATION_DATE).as(Counters.Fields.fullRecalculationDate)
        .andExclude("_id")
    );
    AggregationResults<Document> latestFullRecalculationDateAggregationResults = mongoTemplate.aggregate(aggregation, Campaign.class, Document.class);
    Document latestFullRecalculationDateDocument = latestFullRecalculationDateAggregationResults.getUniqueMappedResult();
    return latestFullRecalculationDateDocument != null ?
      DateUtils.toOffsetDateTime(latestFullRecalculationDateDocument.getDate(Counters.Fields.fullRecalculationDate)) :
      null;
  }

  @Override
  public OffsetDateTime findFirstCampaignStartDate() {
    Aggregation aggregation = Aggregation.newAggregation( // Did not aggregate with MAX because execution looks unstable on Cosmos
      Aggregation.sort(Sort.Direction.ASC, Fields.startDate),
      Aggregation.limit(1),
      Aggregation.project()
        .and(Fields.startDate).as(Fields.startDate)
        .andExclude("_id")
    );
    AggregationResults<Document> firstCampaignStartDateAggregationResults = mongoTemplate.aggregate(aggregation, Campaign.class, Document.class);
    Document firstCampaignStartDateDocument = firstCampaignStartDateAggregationResults.getUniqueMappedResult();
    return firstCampaignStartDateDocument != null ?
      DateUtils.toOffsetDateTime(firstCampaignStartDateDocument.getDate(Fields.startDate)) :
      null;
  }
}
