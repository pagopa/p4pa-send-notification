package it.gov.pagopa.pu.send.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class SendNotificationRepositoryExtImplTest {

  @Mock
  private MongoTemplate mongoTemplate;

  @Mock
  private UpdateResult updateResult;

  @InjectMocks
  private SendNotificationRepositoryExtImpl repository;


  @Test
  void givenUpdateFilePreloadInformationThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    PreLoadResponseDTO preloadResponse = new PreLoadResponseDTO();
    preloadResponse.setPreloadIdx("TEST");
    preloadResponse.setKey("fileKey");
    preloadResponse.setSecret("fileSecret");
    preloadResponse.setHttpMethod(HttpMethodEnum.PUT);
    preloadResponse.setUrl("http://localhost");

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFilePreloadInformation(sendNotificationId, preloadResponse);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class));
  }

  @Test
  void givenUpdateNotificationStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    NotificationStatus newStatus = NotificationStatus.SENDING;

    Mockito.when(mongoTemplate.updateFirst(
      Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationStatus(sendNotificationId, newStatus);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class));
  }

  @Test
  void givenUpdateFileStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    FileStatus newStatus = FileStatus.UPLOADED;

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFileStatus(sendNotificationId, fileName, newStatus);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class));
  }

  @Test
  void givenUpdateFileVersionIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    String versionId = "VERSIONID";

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateFileVersionId(sendNotificationId, fileName, versionId);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class));
  }

  @Test
  void givenUpdateNotificationRequestIdThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "VERSIONID";

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationRequestId(sendNotificationId,notificationRequestId);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class));
  }

  @Test
  void givenUpdateNotificationIunThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String iun = "IUN";

    Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class))).thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateNotificationIun(sendNotificationId,iun);

    assertEquals(1L, result.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(SendNotification.class));
  }
}
