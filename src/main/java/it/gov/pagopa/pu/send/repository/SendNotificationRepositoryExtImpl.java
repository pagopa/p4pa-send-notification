package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotification.Fields;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class SendNotificationRepositoryExtImpl implements SendNotificationRepositoryExt{

  private static final String FIELD_TEMPLATE = "%s.$.%s";

  public static final String FIELD_DOCUMENT_ID = "%s.%s".formatted(Fields.documents, DocumentDTO.Fields.fileName);
  public static final String FIELD_DOCUMENT_KEY = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.key);
  public static final String FIELD_DOCUMENT_URL = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.url);
  public static final String FIELD_DOCUMENT_SECRET = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.secret);
  public static final String FIELD_DOCUMENT_HTTPMETHOD = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.httpMethod);
  public static final String FIELD_DOCUMENT_STATUS = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.status);
  public static final String FIELD_DOCUMENT_VERSIONID = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.versionId);

  private final MongoTemplate mongoTemplate;

  public SendNotificationRepositoryExtImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public UpdateResult updateFilePreloadInformation(String sendNotificationId, PreLoadResponseDTO preloadResponse) {
    return mongoTemplate.updateFirst(
      Query.query(Criteria
        .where(Fields.sendNotificationId).is(sendNotificationId)
          .and(FIELD_DOCUMENT_ID).is(preloadResponse.getPreloadIdx())
      ),
      new Update()
        .set(FIELD_DOCUMENT_KEY, preloadResponse.getKey())
        .set(FIELD_DOCUMENT_SECRET, preloadResponse.getSecret())
        .set(FIELD_DOCUMENT_HTTPMETHOD, preloadResponse.getHttpMethod())
        .set(FIELD_DOCUMENT_URL, preloadResponse.getUrl()),
      SendNotification.class);
  }

  @Override
  public UpdateResult updateNotificationStatus(String sendNotificationId, NotificationStatus newStatus) {
    return mongoTemplate.updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)),
      new Update().set(Fields.status, newStatus),
      SendNotification.class);
  }

  @Override
  public UpdateResult updateNotificationRequestId(String sendNotificationId, String notificationRequestId) {
    return mongoTemplate.updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)),
      new Update().set(Fields.notificationRequestId, notificationRequestId),
      SendNotification.class);
  }

  @Override
  public UpdateResult updateFileStatus(String sendNotificationId, String fileName, FileStatus newStatus) {
    return mongoTemplate.updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_DOCUMENT_ID).is(fileName)),
      new Update().set(FIELD_DOCUMENT_STATUS, newStatus),
      SendNotification.class);
  }

  @Override
  public UpdateResult updateFileVersionId(String sendNotificationId, String fileName, String versionId) {
    return mongoTemplate.updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_DOCUMENT_ID).is(fileName)),
      new Update().set(FIELD_DOCUMENT_VERSIONID, versionId),
      SendNotification.class);
  }
}
