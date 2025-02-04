package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotification.Fields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class SendNotificationRepositoryExtImplTest {

  @Mock
  private MongoTemplate mongoTemplateMock;

  private SendNotificationRepositoryExtImpl repository;

  @BeforeEach
  void init() {
    repository = new SendNotificationRepositoryExtImpl(mongoTemplateMock);
  }

  @AfterEach
  void verifyNotMoreInvocation() {
    Mockito.verifyNoMoreInteractions(mongoTemplateMock);
  }

  @Test
  void whenCreateIfNotExistsThenReturnStoredSendNotification()
  {
    // Given
    Long sendNotificationId = 1L;
    String preloadId = "TEST.pdf";
    String contetType = "application/pdf";
    String sha256 = "ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ";

    CreateNotificationRequest request = CreateNotificationRequest
      .builder()
      .preloadId(preloadId)
      .contentType(contetType)
      .sha256(sha256)
      .build();

    SendNotification sendNotification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .preloadId(preloadId)
      .contentType(contetType)
      .expectedFileDigest(sha256)
      .build();

    Mockito.when(mongoTemplateMock.findAndModify(
        Mockito.eq(Query.query(Criteria.where(Fields.sendNotificationId).is(sendNotificationId)
          .and(Fields.preloadId).is(request.getPreloadId()))),
        Mockito.eq(new Update()
          .setOnInsert(Fields.sendNotificationId, sendNotificationId)
          .setOnInsert(Fields.preloadId, request.getPreloadId())
          .setOnInsert(Fields.expectedFileDigest, request.getSha256())
          .setOnInsert(Fields.contentType, request.getContentType())
          .setOnInsert(Fields.status, NotificationStatus.WAITING_FILE)),
        Mockito.argThat(opt -> opt.isReturnNew() && opt.isUpsert() && !opt.isRemove()),
        Mockito.eq(SendNotification.class)
      )).thenReturn(sendNotification);

    // When
    SendNotification result = repository.createIfNotExists(sendNotificationId, request);
    // Then
    Assertions.assertSame(sendNotification, result);
  }
}
