package it.gov.pagopa.pu.send.service;

import static org.mockito.ArgumentMatchers.any;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.FileUtils;
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
  public void givenStartNotificationRequestWhenStartSendNotificationThenReturnVerifyUpdateFileStatus()
    throws IOException {

    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=",
      fileName);
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
      .status(FileStatus.WAITING)
      .build();

    notification.setDocuments(List.of(documentDTO));

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(Optional.of(notification));

    sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest);

    Mockito.verify(sendNotificationRepositoryMock).updateFileStatus(sendNotificationId,
      fileName, FileStatus.READY);
  }

  @Test
  public void givenStartNotificationRequestWhenStartSendNotificationThenExceptionFileNotFound() {
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
  public void givenStartNotificationRequestWhenStartSendNotificationThenExceptionInvalidSignature()
    throws IOException {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", fileName);
    SendNotification notification = new SendNotification();
    notification.setStatus(NotificationStatus.WAITING_FILE);

    String filePath = "src/main/resources/tmp/" + sendNotificationId + "_"+fileName;
    File file = new File(filePath);
    file.deleteOnExit();

    try (FileWriter writer = new FileWriter(file)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=")
      .contentType("application/pdf")
      .status(FileStatus.WAITING)
      .build();

    notification.setDocuments(List.of(documentDTO));

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(InvalidSignatureException.class, () -> {
      sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest);
    });

    Assertions.assertEquals("File "+fileName+" has not a valid signature", exception.getMessage());
  }
}
