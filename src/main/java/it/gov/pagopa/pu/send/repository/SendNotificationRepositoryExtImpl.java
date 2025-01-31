package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotification.Fields;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SendNotificationRepositoryExtImpl implements SendNotificationRepositoryExt {

  private final MongoTemplate mongoTemplate;

  public SendNotificationRepositoryExtImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public SendNotification createIfNotExists(Long sendNotificationId, SendNotification sendNotification) {

    Query query = Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotification.getSendNotificationId())
      .and(Fields.preloadId).is(sendNotification.getPreloadId()));
    Update update = new Update()
      .setOnInsert(Fields.sendNotificationId, sendNotificationId)
      .setOnInsert(Fields.preloadId, sendNotification.getPreloadId())
      .setOnInsert(Fields.expectedFileDigest, sendNotification.getExpectedFileDigest())
      .setOnInsert(Fields.contentType, sendNotification.getContentType())
      .setOnInsert(Fields.status, NotificationStatus.WAITING_FILE);
    mongoTemplate.upsert(query, update, SendNotification.class);
    return mongoTemplate.findOne(query, SendNotification.class);
  }
}
