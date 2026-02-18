package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.config.BaseEntityListener;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII.Fields;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.OffsetDateTime;
import java.util.Optional;

public class SendNotificationNoPIIRepositoryExtImpl implements SendNotificationNoPIIRepositoryExt {

  private static final String FIELD_TEMPLATE = "%s.$.%s";

  public static final String FIELD_DOCUMENT_ID = "%s.%s".formatted(Fields.documents, DocumentDTO.Fields.fileName);
  public static final String FIELD_DOCUMENT_KEY = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.key);
  public static final String FIELD_DOCUMENT_URL = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.url);
  public static final String FIELD_DOCUMENT_SECRET = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.secret);
  public static final String FIELD_DOCUMENT_HTTPMETHOD = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.httpMethod);
  public static final String FIELD_DOCUMENT_STATUS = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.status);
  public static final String FIELD_DOCUMENT_VERSIONID = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.versionId);
  public static final String FIELD_PAYMENT_NOTICE_CODE = "%s.%s.%s.pagoPa.noticeCode".formatted(Fields.recipients, PuRecipientNoPIIDTO.Fields.puPayments, PuPayment.Fields.payment);
  private static final String FIELD_FILTERED_NOTIFICATION_DATE = "recipients.$[].puPayments.$[elem].notificationDate";

  private final MongoTemplate mongoTemplate;

  public SendNotificationNoPIIRepositoryExtImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  private UpdateResult updateFirst(Query query, Update update) {
    return mongoTemplate.updateFirst(query, BaseEntityListener.setTechFieldsOnDocumentUpdate(update), SendNotificationNoPII.class);
  }

  @Override
  public UpdateResult updateFilePreloadInformation(String sendNotificationId, PreLoadResponseDTO preloadResponse) {
    return updateFirst(
      Query.query(Criteria
        .where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_DOCUMENT_ID).is(preloadResponse.getPreloadIdx())
      ),
      new Update()
        .set(FIELD_DOCUMENT_KEY, preloadResponse.getKey())
        .set(FIELD_DOCUMENT_SECRET, preloadResponse.getSecret())
        .set(FIELD_DOCUMENT_HTTPMETHOD, preloadResponse.getHttpMethod())
        .set(FIELD_DOCUMENT_URL, preloadResponse.getUrl())
      );
  }

  @Override
  public UpdateResult updateNotificationStatus(String sendNotificationId, NotificationStatus newStatus) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)),
      new Update().set(Fields.status, newStatus)
    );
  }

  @Override
  public UpdateResult updateNotificationRequestId(String sendNotificationId, String notificationRequestId) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)),
      new Update().set(Fields.notificationRequestId, notificationRequestId)
    );
  }

  @Override
  public UpdateResult updateFileStatus(String sendNotificationId, String fileName, FileStatus newStatus) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_DOCUMENT_ID).is(fileName)),
      new Update().set(FIELD_DOCUMENT_STATUS, newStatus)
    );
  }

  @Override
  public UpdateResult updateFileVersionId(String sendNotificationId, String fileName, String versionId) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_DOCUMENT_ID).is(fileName)),
      new Update().set(FIELD_DOCUMENT_VERSIONID, versionId)
    );
  }

  @Override
  public UpdateResult updateNotificationIun(String sendNotificationId, String iun) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)),
      new Update()
        .set(Fields.iun, iun)
        .set(Fields.status, NotificationStatus.ACCEPTED)
    );
  }

  @Override
  public UpdateResult updateNotificationDate(String sendNotificationId,
                                             OffsetDateTime notificationDate,
                                             String nav) {
    Query query = new Query();
    query.addCriteria(
      Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_PAYMENT_NOTICE_CODE).is(nav)
    );
    Update update = new Update();
    update.set(FIELD_FILTERED_NOTIFICATION_DATE, notificationDate.toString());
    update.filterArray("elem.payment.pagoPa.noticeCode", nav);

    return updateFirst(query, update);
  }

  @Override
  public Optional<SendNotificationNoPII> findByIdAndOrganizationId(String notificationId, Long organizationId) {
    Query query = new Query();
    query.addCriteria(Criteria.where(Fields.sendNotificationId).is(notificationId)
      .and(Fields.organizationId).is(organizationId));

    return Optional.ofNullable(mongoTemplate.findOne(query, SendNotificationNoPII.class));
  }

  @Override
  public Optional<SendNotificationNoPII> findByOrganizationIdAndNav(Long organizationId, String nav) {
    Query query = new Query();
    query.addCriteria(Criteria.where(Fields.organizationId).is(organizationId)
      .and(FIELD_PAYMENT_NOTICE_CODE).is(nav));

    return Optional.ofNullable(mongoTemplate.findOne(query, SendNotificationNoPII.class));
  }

  @Override
  public Optional<SendNotificationNoPII> findByNotificationRequestId(String notificationRequestId) {
    Query query = new Query();
    query.addCriteria(
      Criteria.where(Fields.notificationRequestId).is(notificationRequestId)
    );

    return Optional.ofNullable(mongoTemplate.findOne(query, SendNotificationNoPII.class));
  }

  @Override
  public UpdateResult addLegalFact(String sendNotificationId, LegalFactDTO legalFact) {
    Query query = Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId));
    Update update = new Update().push(Fields.legalFacts, legalFact);

    return updateFirst(query, update);
  }
}
