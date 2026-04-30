package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationNoPIIRepositoryExtImplTest extends BaseMongoRepositoryTest {

  @Mock
  private UpdateResult updateResult;

  @InjectMocks
  private SendNotificationNoPIIRepositoryExtImpl repository;


  @Test
  void givenUpdateFilePreloadInformationThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    PreLoadResponseDTO preloadResponse = new PreLoadResponseDTO();
    preloadResponse.setPreloadIdx("TEST");
    preloadResponse.setKey("fileKey");
    preloadResponse.setSecret("fileSecret");
    preloadResponse.setHttpMethod(HttpMethodEnum.PUT);
    preloadResponse.setUrl("http://localhost");

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
        SendNotificationNoPII.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFilePreloadInformation(sendNotificationId, preloadResponse);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateNotificationStatusThenVerify() {
    String requestNotificationId = "requestId";
    NotificationStatus newStatus = NotificationStatus.SENDING;

    Mockito.when(mongoTemplateMock.updateFirst(
      Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
          SendNotificationNoPII.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationStatus(requestNotificationId, newStatus);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateNotificationStatusByIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    NotificationStatus newStatus = NotificationStatus.SENDING;

    Mockito.when(mongoTemplateMock.updateFirst(
        Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
          SendNotificationNoPII.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationStatusById(sendNotificationId, newStatus);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateFileStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    FileStatus newStatus = FileStatus.UPLOADED;

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFileStatus(sendNotificationId, fileName, newStatus);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateFileVersionIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    String versionId = "VERSIONID";

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFileVersionId(sendNotificationId, fileName, versionId);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateNotificationRequestIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "VERSIONID";

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationRequestId(sendNotificationId,notificationRequestId);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateNotificationIunThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String iun = "IUN";

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationIun(sendNotificationId,iun);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateNotificationDateThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    OffsetDateTime now = OffsetDateTime.now();

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationDate(sendNotificationId,now,"nav");

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenIdAndOrganizationIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long organizationId = 1L;

    SendNotificationNoPII mockNotification = new SendNotificationNoPII();
    mockNotification.setSendNotificationId(sendNotificationId);
    mockNotification.setOrganizationId(organizationId);

    Mockito.when(mongoTemplateMock.findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(mockNotification);

    Optional<SendNotificationNoPII> result = repository.findByIdAndOrganizationId(sendNotificationId, organizationId);

    assertTrue(result.isPresent());
    assertEquals(sendNotificationId, result.get().getSendNotificationId());
    assertEquals(organizationId, result.get().getOrganizationId());
  }

  @Test
  void givenIdAndOrganizationIdThenReturnEmpty() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long organizationId = 1L;

    Mockito.when(mongoTemplateMock.findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(null);

    Optional<SendNotificationNoPII> result = repository.findByIdAndOrganizationId(sendNotificationId, organizationId);

    assertFalse(result.isPresent());
  }

  @Test
  void givenNotificationRequestIdWhenFindByNotificationRequestIdThenOk() {
    String notificationRequestId = "NOTIFICATION_REQUEST_ID";
    SendNotificationNoPII expectedResult = new SendNotificationNoPII();

    Mockito.when(mongoTemplateMock.findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(expectedResult);

    Optional<SendNotificationNoPII> actualResult = repository.findByNotificationRequestId(notificationRequestId);

    assertTrue(actualResult.isPresent());
    assertEquals(expectedResult, actualResult.get());
  }

  @Test
  void givenAddNotificationLegalFactThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    LegalFactDTO fact = LegalFactDTO.builder().fileName("fileName").build();

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.addLegalFact(sendNotificationId, fact);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateLEgalFactStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String filename = "fileName";
    FileStatus status = FileStatus.EXPIRED;

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateLegalFactStatus(sendNotificationId, filename, status);

    assertEquals(1L, result.getModifiedCount());
  }

}
