package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.config.BaseEntityListener;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.*;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.BaseEntity;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII.Fields;
import it.gov.pagopa.pu.send.util.CampaignUtils;
import it.gov.pagopa.pu.send.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SendNotificationNoPIIRepositoryExtImpl implements SendNotificationNoPIIRepositoryExt {

  private static final String FIELD_TEMPLATE = "%s.$.%s";

  public static final String FIELD_DOCUMENT_ID = "%s.%s".formatted(Fields.documents, DocumentDTO.Fields.fileName);
  public static final String FIELD_DOCUMENT_KEY = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.key);
  public static final String FIELD_DOCUMENT_URL = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.url);
  public static final String FIELD_DOCUMENT_SECRET = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.secret);
  public static final String FIELD_DOCUMENT_HTTPMETHOD = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.httpMethod);
  public static final String FIELD_DOCUMENT_STATUS = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.status);
  public static final String FIELD_DOCUMENT_UPLOAD_DATE = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.uploadDate);
  public static final String FIELD_DOCUMENT_VERSIONID = FIELD_TEMPLATE.formatted(Fields.documents, DocumentDTO.Fields.versionId);
  public static final String FIELD_PAYMENT_NOTICE_CODE = "%s.%s.%s.pagoPa.noticeCode".formatted(Fields.recipients, PuRecipientNoPIIDTO.Fields.puPayments, PuPayment.Fields.payment);
  public static final String FIELD_RECIPIENT_FISCAL_CODE_HASH = "%s.%s".formatted(Fields.recipients, PuRecipientNoPIIDTO.Fields.fiscalCodeHash);
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
  public UpdateResult updateNotificationStatus(String notificationRequestId, NotificationStatus newStatus) {
    return updateFirst(
      Query.query(Criteria.where(Fields.notificationRequestId).is(notificationRequestId)),
      new Update().set(Fields.status, newStatus)
    );
  }

  @Override
  public UpdateResult updateNotificationStatusById(String sendNotificationId, NotificationStatus newStatus) {
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
  public UpdateResult updateFileStatusAndUploadDate(String sendNotificationId, String fileName, FileStatus newStatus, OffsetDateTime uploadDate) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(FIELD_DOCUMENT_ID).is(fileName)),
      new Update()
        .set(FIELD_DOCUMENT_STATUS, newStatus)
        .set(FIELD_DOCUMENT_UPLOAD_DATE, uploadDate)
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

  @Override
  public UpdateResult updateLegalFactStatus(String sendNotificationId, String fileName, FileStatus status) {
    Query query = Query.query(
      Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
        .and(Fields.legalFacts + ".fileName").is(fileName)
    );

    Update update = new Update()
      .set(Fields.legalFacts + ".$.status", status);

    return updateFirst(query, update);
  }

  @Override
  public Counters calculateCampaignCounters(String campaignId) {
    Aggregation aggregation = Aggregation.newAggregation(
      Aggregation.match(Criteria.where(Fields.campaignId).is(campaignId)),
      Aggregation.group()
        .count().as(Counters.Fields.total)
        .sum(buildStatusCondition(Counters.Fields.accepted)).as(Counters.Fields.accepted)
        .sum(buildStatusCondition(Counters.Fields.delivered)).as(Counters.Fields.delivered)
        .sum(buildStatusCondition(Counters.Fields.digitalCompleted)).as(Counters.Fields.digitalCompleted)
        .sum(buildStatusCondition(Counters.Fields.analogicCompleted)).as(Counters.Fields.analogicCompleted)
        .sum(buildStatusCondition(Counters.Fields.completion)).as(Counters.Fields.completion)
    );

    AggregationResults<Counters> results = mongoTemplate.aggregate(aggregation, SendNotificationNoPII.class, Counters.class);

    Counters counters = results.getUniqueMappedResult();

    return counters != null ? counters : new Counters();
  }

  private ConditionalOperators.Cond buildStatusCondition(String counterName) {
    Set<TimelineElementCategoryV27DTO> latestEventsOfInterest =
      CampaignUtils.COUNTER_FIELD2TIMELINE_ELEMENT_CATEGORIES.getOrDefault(counterName, Collections.emptySet());

    return ConditionalOperators.when(Criteria.where(Fields.lastEventOfInterest).in(latestEventsOfInterest))
      .then(1).otherwise(0);
  }

  @Override
  public UpdateResult updateLastEventOfInterestById(String sendNotificationId, TimelineElementCategoryV27DTO newStatus) {
    return updateFirst(
      Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)),
      new Update().set(Fields.lastEventOfInterest, newStatus)
    );
  }

  @Override
  public List<StreamEventSummaryDTO> pushStreamEventsHistory(String sendNotificationId, List<StreamEventSummaryDTO> streamEvents) {
    Query query = new Query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId));
    Update update = BaseEntityListener.setTechFieldsOnDocumentUpdate(new Update().push(Fields.history).each(streamEvents));
    SendNotificationNoPII updatedDoc = mongoTemplate.findAndModify(
      query,
      update,
      FindAndModifyOptions.options().returnNew(true),
      SendNotificationNoPII.class
    );
    return updatedDoc.getHistory();
  }


  @Override
  public Page<SendNotificationNoPII> findSendNotificationsByFilters(SendNotificationFiltersDTO sendNotificationFiltersDTO, Pageable pageable) {
    Query query = new Query();
    query.addCriteria(Criteria
      .where(SendNotificationNoPII.Fields.organizationId).is(sendNotificationFiltersDTO.getOrganizationId())
      .and(Fields.campaignId).is(sendNotificationFiltersDTO.getCampaignId()));
    if(StringUtils.isNotBlank(sendNotificationFiltersDTO.getIun())){
      query.addCriteria(Criteria.where(SendNotificationNoPII.Fields.iun).is(sendNotificationFiltersDTO.getIun()));
    }
    if(sendNotificationFiltersDTO.getDateFrom()!=null && sendNotificationFiltersDTO.getDateTo()!=null){
      query.addCriteria(Criteria.where(BaseEntity.Fields.creationDate)
        .gte(DateUtils.toLocalDateTime(sendNotificationFiltersDTO.getDateFrom()))
        .lte(DateUtils.toLocalDateTime(sendNotificationFiltersDTO.getDateTo())));
    }
    if(!CollectionUtils.isEmpty(sendNotificationFiltersDTO.getStatuses())){
      query.addCriteria(Criteria.where(Fields.status).in(sendNotificationFiltersDTO.getStatuses()));
    }
    if(sendNotificationFiltersDTO.getFiscalCodeHash() != null && sendNotificationFiltersDTO.getFiscalCodeHash().length > 0){
      query.addCriteria(Criteria.where(FIELD_RECIPIENT_FISCAL_CODE_HASH).is(sendNotificationFiltersDTO.getFiscalCodeHash()));
    }

    long count = mongoTemplate.count(query, SendNotificationNoPII.class);
    query.with(pageable);
    List<SendNotificationNoPII> sendNotifications = mongoTemplate.find(query, SendNotificationNoPII.class);
    return new PageImpl<>(sendNotifications, pageable, count);
  }
}
