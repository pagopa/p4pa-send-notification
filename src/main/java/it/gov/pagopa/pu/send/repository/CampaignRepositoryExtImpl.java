package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.config.BaseEntityListener;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.Campaign.Fields;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class CampaignRepositoryExtImpl implements CampaignRepositoryExt {
  private final MongoTemplate mongoTemplate;

  public static final String FIELD_COUNTERS_TEMPLATE = "%s.%s";
  public static final String FIELD_COUNTERS_TOTAL = FIELD_COUNTERS_TEMPLATE.formatted(Fields.counters, Counters.Fields.total);

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
  public Page<Campaign> findCampaignsByFilters(Long organizationId, LocalDate dateFrom, LocalDate dateTo, String orgSubUnitCode, String campaignName, String externalCampaignId, Pageable pageable) {
    Query query = new Query();
    query.addCriteria(Criteria
      .where(Fields.organizationId).is(organizationId)
      .and(Fields.startDate).lte(dateTo)
      .and(Fields.endDate).gte(dateFrom));
    if(StringUtils.isNotBlank(orgSubUnitCode)){
      query.addCriteria(Criteria.where(Fields.orgSubUnitCode).is(orgSubUnitCode));
    }else {
      query.addCriteria(Criteria.where(Fields.orgSubUnitCode).is(null));
    }
    if(StringUtils.isNotBlank(campaignName)){
      query.addCriteria(Criteria.where(Fields.campaignName).regex(Pattern.quote(campaignName), "i"));
    }
    if(StringUtils.isNotBlank(externalCampaignId)){
      query.addCriteria(Criteria.where(Fields.externalId).is(externalCampaignId));
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
}
