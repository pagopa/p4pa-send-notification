package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotification.Fields;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
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
  public SendNotification createIfNotExists(Long sendNotificationId, CreateNotificationRequest notificationRequest) {
    return mongoTemplate.findAndModify(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
      .and(Fields.preloadId).is(notificationRequest.getPreloadId())),
      new Update()
        .setOnInsert(Fields.sendNotificationId, sendNotificationId)
        .setOnInsert(Fields.preloadId, notificationRequest.getPreloadId())
        .setOnInsert(Fields.expectedFileDigest, notificationRequest.getSha256())
        .setOnInsert(Fields.contentType, notificationRequest.getContentType())
        .setOnInsert(Fields.status, NotificationStatus.WAITING_FILE),
      FindAndModifyOptions.options()
        .returnNew(true)
        .upsert(true),
      SendNotification.class
      );
  }
}
