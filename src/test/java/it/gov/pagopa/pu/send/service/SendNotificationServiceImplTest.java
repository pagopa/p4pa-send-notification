package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceImplTest {

  @Mock
  private SendNotificationRepository sendNotificationRepositoryMock;

  @Mock
  private CreateNotificationRequest2SendNotificationMapper mapper;

  @InjectMocks
  private SendNotificationServiceImpl sendNotificationService;

  @Test
  void givenCreateNotificationRequestWhenCreateSendNotificationThenReturnCreateNotificationResponse(){
    // Given
    CreateNotificationRequest request = new CreateNotificationRequest();
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("SENDNOTIFICATIONID");
    sendNotification.setStatus(NotificationStatus.WAITING_FILE);

    // When
    Mockito.when(mapper.map(request)).thenReturn(sendNotification);
    Mockito.when(sendNotificationRepositoryMock.insert(sendNotification)).thenReturn(sendNotification);

    CreateNotificationResponse response = sendNotificationService.createSendNotification(request);

    // Then
    Mockito.verify(sendNotificationRepositoryMock).insert(sendNotification);
    Assertions.assertNotNull(response);
    Assertions.assertEquals("SENDNOTIFICATIONID", response.getSendNotificationId());
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenReturnVerifyAllFilesReady()
    throws IOException {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=",fileName);
    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.WAITING);
    SendNotification updatedNotification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification))
      .thenReturn(Optional.of(updatedNotification));

    sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest);

    Mockito.verify(sendNotificationRepositoryMock).updateFileStatus(sendNotificationId, fileName, FileStatus.READY);
    Mockito.verify(sendNotificationRepositoryMock).updateNotificationStatus(sendNotificationId, NotificationStatus.SENDING);
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionFileNotFound() {
    String sendNotificationId = "sendNotificationId";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", "NOTEXISTS");
    SendNotification notification = new SendNotification();
    notification.setStatus(NotificationStatus.WAITING_FILE);
    notification.setDocuments(List.of());

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest);
    });

    Assertions.assertEquals("File not found with id: NOTEXISTS", exception.getMessage());
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionInvalidSignature()
    throws IOException {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", fileName);
    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.WAITING);

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(InvalidSignatureException.class, () -> {
      sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest);
    });

    Assertions.assertEquals("File "+fileName+" has not a valid signature", exception.getMessage());
  }

  @Test
  void givenDeleteNotificationRequestWhenDeleteSendNotificationThenVerify()
    throws IOException {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";

    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    notification.setStatus(NotificationStatus.COMPLETE);
    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    sendNotificationService.deleteSendNotification(sendNotificationId);
    Mockito.verify(sendNotificationRepositoryMock).deleteById(sendNotificationId);
  }

  @Test
  void givenDeleteNotificationRequestWithStatusCompleteWhenDeleteSendNotificationThenInvalidStatusException()
    throws IOException {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";

    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    notification.setStatus(NotificationStatus.COMPLETE);
    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(InvalidStatusException.class, () -> sendNotificationService.deleteSendNotification(sendNotificationId));
    Assertions.assertEquals("Cannot delete notification with status complete", exception.getMessage());
  }

  private SendNotification createMockNotification(String sendNotificationId, String fileName, FileStatus fileStatus)
    throws IOException {

    SendNotification notification = new SendNotification();
    notification.setStatus(NotificationStatus.WAITING_FILE);

    String filePath = "src/main/resources/tmp/" + sendNotificationId + "_"+ fileName;
    File file = new File(filePath);
    file.deleteOnExit();

    try (FileWriter writer = new FileWriter(file)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=")
      .contentType("application/pdf")
      .status(fileStatus)
      .build();

    notification.setDocuments(List.of(documentDTO));

    return notification;
  }


}
