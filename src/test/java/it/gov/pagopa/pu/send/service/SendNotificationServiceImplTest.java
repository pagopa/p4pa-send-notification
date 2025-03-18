package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.workflow.service.WorkflowService;
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

import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
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

  @Mock
  private WorkflowService workflowServiceMock;

  @InjectMocks
  private SendNotificationServiceImpl sendNotificationService;

  @Test
  void givenCreateNotificationRequestWhenCreateSendNotificationThenReturnCreateNotificationResponse(){
    // Given
    CreateNotificationRequest request = new CreateNotificationRequest();
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("SENDNOTIFICATIONID");
    sendNotification.setStatus(NotificationStatus.WAITING_FILE);
    Long organizationId = 1L;

    // When
    Mockito.when(mapper.map(request, organizationId)).thenReturn(sendNotification);
    Mockito.when(sendNotificationRepositoryMock.insert(sendNotification)).thenReturn(sendNotification);

    CreateNotificationResponse response = sendNotificationService.createSendNotification(request, organizationId);

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
    Long organizationId = 1L;
    WorkflowCreatedDTO workflow = WorkflowCreatedDTO.builder().workflowId("WORKFLOWID").build();

    Mockito.when(sendNotificationRepositoryMock.findByIdAndOrganizationId(sendNotificationId, organizationId))
      .thenReturn(Optional.of(notification))
      .thenReturn(Optional.of(updatedNotification));
    Mockito.when(workflowServiceMock.sendNotificationProcess(sendNotificationId, null))
        .thenReturn(workflow);

    sendNotificationService.startSendNotification(sendNotificationId, organizationId, loadFileRequest, null);

    Mockito.verify(sendNotificationRepositoryMock).updateFileStatus(sendNotificationId, fileName, FileStatus.READY);
    Mockito.verify(sendNotificationRepositoryMock).updateNotificationStatus(sendNotificationId, NotificationStatus.SENDING);
    Mockito.verify(workflowServiceMock).sendNotificationProcess(sendNotificationId, null);
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionFileNotFound() {
    String sendNotificationId = "sendNotificationId";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", "NOTEXISTS");
    SendNotification notification = new SendNotification();
    notification.setStatus(NotificationStatus.WAITING_FILE);
    notification.setDocuments(List.of());
    Long organizationId = 1L;

    Mockito.when(sendNotificationRepositoryMock.findByIdAndOrganizationId(sendNotificationId, organizationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> sendNotificationService.startSendNotification(sendNotificationId, organizationId, loadFileRequest, null));

    Assertions.assertEquals("File not found with id: NOTEXISTS", exception.getMessage());
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionInvalidSignature()
    throws IOException {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", fileName);
    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.WAITING);
    Long organizationId = 1L;

    Mockito.when(sendNotificationRepositoryMock.findByIdAndOrganizationId(sendNotificationId, organizationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(InvalidSignatureException.class, () -> sendNotificationService.startSendNotification(sendNotificationId, organizationId, loadFileRequest, null));

    Assertions.assertEquals("File "+fileName+" has not a valid signature", exception.getMessage());
  }

  @Test
  void givenDeleteNotificationRequestWhenDeleteSendNotificationThenVerify()
    throws IOException {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    Long organizationId = 1L;
    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    //When
    Mockito.when(sendNotificationRepositoryMock.findByIdAndOrganizationId(sendNotificationId, organizationId)).thenReturn(
      Optional.of(notification));
    //Then
    sendNotificationService.deleteSendNotification(sendNotificationId, organizationId);
    Mockito.verify(sendNotificationRepositoryMock).deleteById(sendNotificationId);
  }

  @Test
  void givenDeleteNotificationRequestWithStatusCompleteWhenDeleteSendNotificationThenInvalidStatusException()
    throws IOException {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    Long organizationId = 1L;
    SendNotification notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    notification.setStatus(NotificationStatus.COMPLETE);
    //When
    Mockito.when(sendNotificationRepositoryMock.findByIdAndOrganizationId(sendNotificationId, organizationId)).thenReturn(
      Optional.of(notification));
    //Then
    Exception exception = Assertions.assertThrows(InvalidStatusException.class, () -> sendNotificationService.deleteSendNotification(sendNotificationId, organizationId));
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
