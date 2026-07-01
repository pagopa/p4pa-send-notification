package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.config.BaseEntityListener;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.Campaign.Fields;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDate;

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
}
