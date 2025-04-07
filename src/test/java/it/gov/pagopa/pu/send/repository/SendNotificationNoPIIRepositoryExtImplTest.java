package it.gov.pagopa.pu.send.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SendNotificationNoPIIRepositoryExtImplTest {

  @Mock
  private MongoTemplate mongoTemplate;

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

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
        SendNotificationNoPII.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFilePreloadInformation(sendNotificationId, preloadResponse);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenUpdateNotificationStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    NotificationStatus newStatus = NotificationStatus.SENDING;

    Mockito.when(mongoTemplate.updateFirst(
      Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
          SendNotificationNoPII.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationStatus(sendNotificationId, newStatus);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenUpdateFileStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    FileStatus newStatus = FileStatus.UPLOADED;

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFileStatus(sendNotificationId, fileName, newStatus);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenUpdateFileVersionIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    String versionId = "VERSIONID";

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFileVersionId(sendNotificationId, fileName, versionId);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenUpdateNotificationRequestIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "VERSIONID";

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationRequestId(sendNotificationId,notificationRequestId);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenUpdateNotificationIunThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String iun = "IUN";

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationIun(sendNotificationId,iun);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenUpdateNotificationDateThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    OffsetDateTime now = OffsetDateTime.now();

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationDate(sendNotificationId,now);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenIdAndOrganizationIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long organizationId = 1L;

    SendNotificationNoPII mockNotification = new SendNotificationNoPII();
    mockNotification.setSendNotificationId(sendNotificationId);
    mockNotification.setOrganizationId(organizationId);

    Mockito.when(mongoTemplate.findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(mockNotification);

    Optional<SendNotificationNoPII> result = repository.findByIdAndOrganizationId(sendNotificationId, organizationId);

    assertTrue(result.isPresent());
    assertEquals(sendNotificationId, result.get().getSendNotificationId());
    assertEquals(organizationId, result.get().getOrganizationId());

    Mockito.verify(mongoTemplate, Mockito.times(1)).findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenIdAndOrganizationIdThenReturnEmpty() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long organizationId = 1L;

    Mockito.when(mongoTemplate.findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(null);

    Optional<SendNotificationNoPII> result = repository.findByIdAndOrganizationId(sendNotificationId, organizationId);

    assertFalse(result.isPresent());

    Mockito.verify(mongoTemplate, Mockito.times(1)).findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class));
  }

  @Test
  void givenOrganizationIdAndIUVThenVerify() {
    String iuv = "IUV";
    Long organizationId = 1L;
    Payment payment = new Payment();
    PagoPa pagoPa = new PagoPa();
    pagoPa.setNoticeCode("3"+iuv);
    payment.setPagoPa(pagoPa);

    SendNotificationNoPII mockNotification = new SendNotificationNoPII();
    mockNotification.setPayments(Collections.singletonList(new PuPayment(1L, payment)));
    mockNotification.setOrganizationId(organizationId);

    Mockito.when(mongoTemplate.findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class))).thenReturn(mockNotification);

    Optional<SendNotificationNoPII> result = repository.findByOrganizationIdAndIUV(organizationId, iuv);

    assertTrue(result.isPresent());
    assertEquals("3"+iuv, result.get().getPayments().getFirst().getPayment().getPagoPa().getNoticeCode());
    assertEquals(organizationId, result.get().getOrganizationId());

    Mockito.verify(mongoTemplate, Mockito.times(1)).findOne(Mockito.any(Query.class), Mockito.eq(
      SendNotificationNoPII.class));

  }
}
