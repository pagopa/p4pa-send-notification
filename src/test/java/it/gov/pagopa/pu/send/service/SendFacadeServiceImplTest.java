package it.gov.pagopa.pu.send.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendFacadeServiceImplTest {
  @Mock
  private SendNotificationRepository sendNotificationRepository;

  @Mock
  private SendClient sendClient;

  @Mock
  private SendUploadFacadeServiceImpl uploadService;

  @Mock
  private SendNotification2NewNotificationRequestMapper sendNotificationMapper;

  @InjectMocks
  private SendFacadeServiceImpl sendService;


  @Test
  void givenValidNotificationWhenPreloadFilesThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("digest123")
      .contentType("application/pdf")
      .status(FileStatus.READY)
      .build();
    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .status(NotificationStatus.SENDING)
      .documents(List.of(documentDTO))
      .build();

    PreLoadRequestDTO preLoadRequestDTO = new PreLoadRequestDTO();
    preLoadRequestDTO.sha256("digest123");
    preLoadRequestDTO.setContentType("application/pdf");
    preLoadRequestDTO.setPreloadIdx(fileName);

    PreLoadResponseDTO preLoadResponse = new PreLoadResponseDTO();
    preLoadResponse.setPreloadIdx(fileName);
    preLoadResponse.setKey("fileKey");
    preLoadResponse.setSecret("fileSecret");
    preLoadResponse.setHttpMethod(HttpMethodEnum.POST);
    preLoadResponse.setUrl("http://example.com");

    Mockito.when(sendNotificationRepository.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Mockito.when(sendClient.preloadFiles(List.of(preLoadRequestDTO))).thenReturn(List.of(preLoadResponse));

    sendService.preloadFiles(sendNotificationId);

    Mockito.verify(sendNotificationRepository, Mockito.times(1)).updateFilePreloadInformation(eq(sendNotificationId), any(PreLoadResponseDTO.class));
    Mockito.verify(sendNotificationRepository, Mockito.times(1)).updateNotificationStatus(sendNotificationId, NotificationStatus.REGISTERED);
  }

  @Test
  void givenNotExistsNotificationWhenPreloadFilesThenException() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Mockito.when(sendNotificationRepository.findById(sendNotificationId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(IllegalArgumentException.class, () -> sendService.preloadFiles(sendNotificationId));

    assertEquals("Notification not found with id: " + sendNotificationId, exception.getMessage());
  }

  @Test
  void givenValidNotificationWhenUploadFilesThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    String versionId = "VERSIONID";

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("digest123")
      .contentType("application/pdf")
      .status(FileStatus.READY)
      .httpMethod("PUT")
      .key("KEY")
      .url("URL")
      .secret("SECRET")
      .build();

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .status(NotificationStatus.REGISTERED)
      .documents(List.of(documentDTO))
      .build();

    Mockito.when(sendNotificationRepository.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));
    Mockito.when(uploadService.uploadFile(sendNotificationId, documentDTO)).thenReturn(Optional.of(versionId));

    sendService.uploadFiles(sendNotificationId);
    Mockito.verify(sendNotificationRepository, Mockito.times(1)).updateFileVersionId(sendNotificationId, fileName, versionId);
    Mockito.verify(sendNotificationRepository, Mockito.times(1)).updateFileStatus(sendNotificationId, fileName, FileStatus.UPLOADED);
  }

  @Test
  void givenValidNotificationWhenDeliveryNotificationThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";

    NewNotificationResponseDTO response = new NewNotificationResponseDTO();
    response.setNotificationRequestId("NOTIFICATIONREQUESTID");

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .status(NotificationStatus.UPLOADED)
      .build();

    Mockito.when(sendNotificationRepository.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendClient.deliveryNotification(sendNotificationMapper.apply(notification))).thenReturn(response);

    sendService.deliveryNotification(sendNotificationId);

    Mockito.verify(sendNotificationRepository, Mockito.times(1))
      .updateNotificationRequestId(sendNotificationId, response.getNotificationRequestId());
    Mockito.verify(sendNotificationRepository, Mockito.times(1))
      .updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
  }
}
