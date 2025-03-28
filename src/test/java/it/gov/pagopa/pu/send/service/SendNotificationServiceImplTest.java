package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.citizen.model.PersonalData;
import it.gov.pagopa.pu.send.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.SendNotificationFileNotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceImplTest {

  @Mock
  private SendNotificationRepository sendNotificationRepositoryMock;
  @Mock
  private CreateNotificationRequest2SendNotificationMapper mapperMock;
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private FileRetrieverService fileRetrieverServiceMock;
  @Mock
  private SendNotification2SendNotificationDTOMapper sendNotificationDTOMapperMock;
  @Mock
  private DataCipherService dataCipherServiceMock;
  @Mock
  private PersonalDataRepository personalDataRepositoryMock;

  @InjectMocks
  private SendNotificationServiceImpl sendNotificationService;

  @Test
  void givenCreateNotificationRequestWhenCreateSendNotificationThenReturnCreateNotificationResponse(){
    // Given
    CreateNotificationRequest request = new CreateNotificationRequest();
    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();
    sendNotificationNoPII.setSendNotificationId("SENDNOTIFICATIONID");
    sendNotificationNoPII.setStatus(NotificationStatus.WAITING_FILE);
    String accessToken = "accessToken";
    byte[] encryptedObj = "OBJ".getBytes();

    SendNotificationPIIDTO sendNotificationPIIDTO = new SendNotificationPIIDTO();
    PersonalData personalData = new PersonalData();
    personalData.setId(1L);

    Mockito.when(mapperMock.mapToNoPII(request, accessToken)).thenReturn(sendNotificationNoPII);
    Mockito.when(mapperMock.mapToPii(request)).thenReturn(sendNotificationPIIDTO);
    Mockito.when(dataCipherServiceMock.encryptObj(Mockito.any(SendNotificationPIIDTO.class))).thenReturn(encryptedObj);
    Mockito.when(personalDataRepositoryMock.save(Mockito.any(PersonalData.class))).thenReturn(personalData);
    Mockito.when(sendNotificationRepositoryMock.insert(sendNotificationNoPII)).thenReturn(
      sendNotificationNoPII);

    // When
    CreateNotificationResponse response = sendNotificationService.createSendNotification(request, accessToken);

    // Then
    Mockito.verify(sendNotificationRepositoryMock).insert(sendNotificationNoPII);
    Assertions.assertNotNull(response);
    Assertions.assertEquals("SENDNOTIFICATIONID", response.getSendNotificationId());
  }


  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenReturnVerifyAllFilesReady() {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=",fileName);
    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.WAITING);
    SendNotificationNoPII updatedNotification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    WorkflowCreatedDTO workflow = WorkflowCreatedDTO.builder().workflowId("WORKFLOWID").build();
    InputStream inputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification))
      .thenReturn(Optional.of(updatedNotification));
    Mockito.when(fileRetrieverServiceMock.retrieveFile(notification.getOrganizationId(), sendNotificationId+"_"+fileName)).thenReturn(inputStream);
    Mockito.when(workflowServiceMock.sendNotificationProcess(sendNotificationId, null))
        .thenReturn(workflow);

    sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, null);

    Mockito.verify(sendNotificationRepositoryMock).updateFileStatus(sendNotificationId, fileName, FileStatus.READY);
    Mockito.verify(sendNotificationRepositoryMock).updateNotificationStatus(sendNotificationId, NotificationStatus.SENDING);
    Mockito.verify(workflowServiceMock).sendNotificationProcess(sendNotificationId, null);
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionFileNotFound() {
    String sendNotificationId = "sendNotificationId";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", "NOTEXISTS");
    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setStatus(NotificationStatus.WAITING_FILE);
    notification.setDocuments(List.of());

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(SendNotificationFileNotFoundException.class, () -> sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, null));

    Assertions.assertEquals("File not found with id: NOTEXISTS", exception.getMessage());
  }


  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionInvalidSignature() {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", fileName);
    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.WAITING);
    InputStream inputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    Mockito.when(fileRetrieverServiceMock.retrieveFile(notification.getOrganizationId(), sendNotificationId+"_"+fileName)).thenReturn(inputStream);
    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Exception exception = Assertions.assertThrows(InvalidSignatureException.class, () -> sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, null));

    Assertions.assertEquals("File "+fileName+" has not a valid signature", exception.getMessage());
  }


  @Test
  void givenDeleteNotificationRequestWhenDeleteSendNotificationThenVerify() {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    //When
    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));
    //Then
    sendNotificationService.deleteSendNotification(sendNotificationId);
    Mockito.verify(sendNotificationRepositoryMock).deleteById(sendNotificationId);
  }

  @Test
  void givenDeleteNotificationRequestWithStatusCompleteWhenDeleteSendNotificationThenInvalidStatusException() {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    notification.setStatus(NotificationStatus.COMPLETE);

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    //When
    Exception exception = Assertions.assertThrows(InvalidStatusException.class, () -> sendNotificationService.deleteSendNotification(sendNotificationId));

    //Then
    Assertions.assertEquals("Cannot delete notification with status complete", exception.getMessage());
  }

  private SendNotificationNoPII createMockNotification(String sendNotificationId, String fileName, FileStatus fileStatus) {
    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setOrganizationId(1L);
    notification.setSendNotificationId(sendNotificationId);
    notification.setStatus(NotificationStatus.WAITING_FILE);

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=")
      .contentType("application/pdf")
      .status(fileStatus)
      .build();

    notification.setDocuments(List.of(documentDTO));

    return notification;
  }

  @Test
  void givenExistentNotificationWhenFindSendNotificationDTOThenReturnIt(){
    // Given
    String sendNotificationId = "NOTIFICATIONID";
    SendNotificationNoPII notification = new SendNotificationNoPII();
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId))
        .thenReturn(Optional.of(notification));
    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.same(notification)))
      .thenReturn(expectedResult);

    // When
    SendNotificationDTO result = sendNotificationService.findSendNotificationDTO(sendNotificationId);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentNotificationWhenFindSendNotificationDTOThenThrowotFoundException(){
    // Given
    String sendNotificationId = "NOTIFICATIONID";

    Mockito.when(sendNotificationRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(SendNotificationNotFoundException.class, () -> sendNotificationService.findSendNotificationDTO(sendNotificationId));
  }

}
