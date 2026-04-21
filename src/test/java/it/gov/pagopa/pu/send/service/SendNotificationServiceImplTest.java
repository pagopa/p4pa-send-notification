package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.service.BrokerConfigurationService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.*;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationPIIRepository;
import it.gov.pagopa.pu.send.util.TestUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceImplTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private SendNotificationPIIRepository sendNotificationPIIRepositoryMock;
  @Mock
  private CreateNotificationRequest2SendNotificationMapper mapperMock;
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private SendNotification2SendNotificationDTOMapper sendNotificationDTOMapperMock;
  @Mock
  private BrokerConfigurationService brokerConfigurationServiceMock;

  @InjectMocks
  private SendNotificationServiceImpl sendNotificationService;

  @Test
  void givenCreateNotificationRequestWhenCreateSendNotificationThenReturnCreateNotificationResponse(){
    // Given
    CreateNotificationRequest request = new CreateNotificationRequest();
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("SENDNOTIFICATIONID");
    sendNotification.setStatus(NotificationStatus.WAITING_FILE);
    String accessToken = "accessToken";

    PersonalData personalData = new PersonalData();
    personalData.setId(1L);

    Mockito.when(mapperMock.mapToModel(request, accessToken)).thenReturn(sendNotification);
    Mockito.when(sendNotificationPIIRepositoryMock.save(Mockito.any(SendNotification.class))).thenReturn(sendNotification);


    // When
    CreateNotificationResponse response = sendNotificationService.createSendNotification(request, accessToken);

    // Then
    Mockito.verify(sendNotificationPIIRepositoryMock).save(sendNotification);
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
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");
    InputStream inputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification))
      .thenReturn(Optional.of(updatedNotification));
    Mockito.when(fileStorerServiceMock.retrieveFile(notification.getOrganizationId(), sendNotificationId, sendNotificationId+"_"+fileName)).thenReturn(inputStream);
    Mockito.when(workflowServiceMock.sendNotificationProcess(sendNotificationId, null))
        .thenReturn(workflow);

    StartNotificationResponse result = sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, null);

    Assertions.assertEquals(new StartNotificationResponse("workflowId", "runId"), result);

    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateFileStatus(sendNotificationId, fileName, FileStatus.READY);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateNotificationStatusById(sendNotificationId, NotificationStatus.SENDING);
    Mockito.verify(workflowServiceMock).sendNotificationProcess(sendNotificationId, null);
  }

  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionFileNotFound() {
    String sendNotificationId = "sendNotificationId";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", "NOTEXISTS");
    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setStatus(NotificationStatus.WAITING_FILE);
    notification.setDocuments(List.of());

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    SendNotificationFileNotFoundException exception = Assertions.assertThrows(SendNotificationFileNotFoundException.class, () -> sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, null));

    Assertions.assertEquals("FILE_NOT_FOUND",exception.getCode());
    Assertions.assertEquals("File not found with id: NOTEXISTS", exception.getMessage());
  }


  @Test
  void givenStartNotificationRequestWhenStartSendNotificationThenExceptionInvalidSignature() {
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    LoadFileRequest loadFileRequest = new LoadFileRequest("DIGEST", fileName);
    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.WAITING);
    InputStream inputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    Mockito.when(fileStorerServiceMock.retrieveFile(notification.getOrganizationId(), sendNotificationId,sendNotificationId+"_"+fileName)).thenReturn(inputStream);
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    InvalidSignatureException exception = Assertions.assertThrows(InvalidSignatureException.class, () -> sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, null));

    Assertions.assertEquals("INVALID_SIGNATURE",exception.getCode());
    Assertions.assertEquals("File "+fileName+" has not a valid signature", exception.getMessage());
  }


  @Test
  void givenDeleteNotificationRequestWhenDeleteSendNotificationThenVerify() {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    Path relativePath = Path.of("1/sendNotificationId");

    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    PuPayment puPayment = new PuPayment();
    Payment payment = new Payment();
    PagoPa pagoPa = new PagoPa();
    Attachment attachment = new Attachment();
    attachment.setFileName("FILENAME");
    pagoPa.setAttachment(attachment);
    F24Payment f24 = new F24Payment();
    f24.setMetadataAttachment(attachment);
    payment.setF24(f24);
    payment.setPagoPa(pagoPa);
    puPayment.setPayment(payment);
    notification.getRecipients().getFirst().setPuPayments(List.of(puPayment));

    //When
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));
    Mockito.when(fileStorerServiceMock.buildRelativeSendPath(
      notification.getOrganizationId(), sendNotificationId)).thenReturn(relativePath);
    //Then
    sendNotificationService.deleteSendNotification(sendNotificationId);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).deleteById(sendNotificationId);

  }

  @Test
  void givenDeleteNotificationRequestWhenDeleteSendNotificationThenDeleteFileException() {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    Path relativePath = Path.of("1/sendNotificationId");

    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    PuPayment puPayment = new PuPayment();
    Payment payment = new Payment();
    PagoPa pagoPa = new PagoPa();
    Attachment attachment = new Attachment();
    attachment.setFileName("FILENAME");
    pagoPa.setAttachment(attachment);
    F24Payment f24 = new F24Payment();
    f24.setMetadataAttachment(attachment);
    payment.setF24(f24);
    payment.setPagoPa(pagoPa);
    puPayment.setPayment(payment);
    notification.getRecipients().getFirst().setPuPayments(List.of(puPayment));

    //When
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));
    Mockito.when(fileStorerServiceMock.buildRelativeSendPath(
      notification.getOrganizationId(), sendNotificationId)).thenReturn(relativePath);
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("DUMMY"));
      //Then
      Assertions.assertThrows(DeleteFileException.class, () -> sendNotificationService.deleteSendNotification(sendNotificationId));
    }
  }

  @Test
  void givenDeleteNotificationRequestWithStatusCompleteWhenDeleteSendNotificationThenInvalidStatusException() {
    //Given
    String sendNotificationId = "sendNotificationId";
    String fileName = "file.pdf";
    SendNotificationNoPII notification = createMockNotification(sendNotificationId, fileName, FileStatus.READY);
    notification.setStatus(NotificationStatus.ACCEPTED);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
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
    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO();
    notification.setRecipients(List.of(recipient));

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

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
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

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(SendNotificationNotFoundException.class, () -> sendNotificationService.findSendNotificationDTO(sendNotificationId));
  }

  @Test
  void givenExistentNotificationWhenFindSendNotificationDTOByNotificationRequestIdThenReturnIt(){
    // Given
    String notificationRequestId = "NOTIFICATION_REQUEST_ID";
    SendNotificationNoPII notification = new SendNotificationNoPII();
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findByNotificationRequestId(notificationRequestId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.same(notification)))
      .thenReturn(expectedResult);

    // When
    SendNotificationDTO result = sendNotificationService.findSendNotificationDTOByNotificationRequestId(notificationRequestId);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentNotificationWhenFindSendNotificationDTOThenThrowFoundException(){
    // Given
    String notificationRequestId = "NOTIFICATION_REQUEST_ID";

    Mockito.when(sendNotificationNoPIIRepositoryMock.findByNotificationRequestId(notificationRequestId))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(SendNotificationNotFoundException.class, () -> sendNotificationService.findSendNotificationDTOByNotificationRequestId(notificationRequestId));
  }

  @Test
  void givenExistentNotificationWhenFindSendNotificationByOrgIdAndNavThenReturnIt(){
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    SendNotificationNoPII notification = new SendNotificationNoPII();
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findByOrganizationIdAndNav(organizationId, nav))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.same(notification)))
      .thenReturn(expectedResult);

    // When
    SendNotificationDTO result = sendNotificationService.findSendNotificationByOrgIdAndNav(organizationId, nav);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentNotificationWhenFindSendNotificationByOrgIdAndNavThenThrowNotFoundException(){
    // Given
    Long organizationId = 1L;
    String nav = "NAV";

    Mockito.when(sendNotificationNoPIIRepositoryMock.findByOrganizationIdAndNav(organizationId, nav))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(SendNotificationNotFoundException.class, () ->
      sendNotificationService.findSendNotificationByOrgIdAndNav(organizationId, nav));
  }

  @Test
  void whenUpdateNotificationStatus_thenInvokeRepositoryAndReturnResult() {
    // Given
    String requestId = "REQUESTID";
    NotificationStatus status = NotificationStatus.REFUSED;
    UpdateResult expectedResult = UpdateResult.acknowledged(1, 1L, null);

    Mockito.when(sendNotificationNoPIIRepositoryMock.updateNotificationStatus(requestId, status))
      .thenReturn(expectedResult);

    // When
    UpdateResult result = sendNotificationService.updateNotificationStatus(requestId, status);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.wasAcknowledged());
    Assertions.assertEquals(1, result.getMatchedCount());
    Assertions.assertEquals(1, result.getModifiedCount());
    Assertions.assertSame(expectedResult, result);

    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateNotificationStatus(requestId, status);
  }

  @Test
  void givenNewFileWhenUploadSendLegalFactThenSaveFileAndInvokeRepository() {
    // Given
    String notificationId = "123";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.SENDER_ACK;
    String fileName = "test.pdf";
    InputStream inputStreamMock = Mockito.mock(InputStream.class);
    String expectedUrl = "test.pdf";

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(notificationId);
    notification.setOrganizationId(1L);
    notification.setLegalFacts(new ArrayList<>());

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(notificationId)).thenReturn(Optional.of(notification));
    Mockito.when(fileStorerServiceMock.saveToSharedFolder(1L, notificationId, inputStreamMock, fileName))
      .thenReturn(expectedUrl);

    // When
    Assertions.assertDoesNotThrow(() ->
      sendNotificationService.uploadSendLegalFact(notificationId, category, fileName, inputStreamMock)
    );

    // Then
    Mockito.verify(fileStorerServiceMock).saveToSharedFolder(1L, notificationId, inputStreamMock, fileName);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).addLegalFact(eq(notificationId), argThat(fact ->
      fact.getFileName().equals(fileName) &&
        fact.getUrl().equals(expectedUrl) &&
        fact.getCategory().equals(category)
    ));
  }

  @Test
  void givenNewFileWhenUploadSendLegalFactWithoutLegalFactThenSaveFileAndInvokeRepository() {
    // Given
    String notificationId = "123";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.SENDER_ACK;
    String fileName = "test.pdf";
    InputStream inputStreamMock = Mockito.mock(InputStream.class);
    String expectedUrl = "test.pdf";

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(notificationId);
    notification.setOrganizationId(1L);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(notificationId)).thenReturn(Optional.of(notification));
    Mockito.when(fileStorerServiceMock.saveToSharedFolder(1L, notificationId, inputStreamMock, fileName))
      .thenReturn(expectedUrl);

    // When
    Assertions.assertDoesNotThrow(() ->
      sendNotificationService.uploadSendLegalFact(notificationId, category, fileName, inputStreamMock)
    );

    // Then
    Mockito.verify(fileStorerServiceMock).saveToSharedFolder(1L, notificationId, inputStreamMock, fileName);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).addLegalFact(eq(notificationId), argThat(fact ->
      fact.getFileName().equals(fileName) &&
        fact.getUrl().equals(expectedUrl) &&
        fact.getCategory().equals(category)
    ));
  }

  @Test
  void givenExistingCategoryFileWhenUploadSendLegalFactThenThrowFileAlreadyExistsException() {
    // Given
    String id = "123";
    String fileName = "file.pdf";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.SENDER_ACK;
    InputStream inputStreamMock = Mockito.mock(InputStream.class);

    LegalFactDTO existingFact = LegalFactDTO.builder()
      .category(category)
      .fileName(fileName)
      .build();
    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setLegalFacts(List.of(existingFact));

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(id)).thenReturn(Optional.of(notification));

    // When & Then
    FileAlreadyExistsException exception = Assertions.assertThrows(FileAlreadyExistsException.class, () ->
      sendNotificationService.uploadSendLegalFact(id, category, fileName, inputStreamMock)
    );

    Assertions.assertEquals("LEGAL_FACT_ALREADY_EXISTS", exception.getCode());

    Mockito.verifyNoInteractions(fileStorerServiceMock);
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.never()).addLegalFact(any(), any());
  }

  @Test
  void givenSendNotificationIdWhenGetLegalFactsThenOk() {
    // Given
    String notificationId = "123";

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(notificationId);
    notification.setOrganizationId(1L);
    notification.setLegalFacts(List.of(LegalFactDTO.builder()
      .fileName("FILENAME")
      .url("URL")
      .category(LegalFactCategoryDTO.SENDER_ACK)
      .build()));

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(notificationId)).thenReturn(Optional.of(notification));

    List<LegalFactDTO> response = sendNotificationService.getLegalFacts(notificationId);

    // When
    Assertions.assertNotNull(response);
    Assertions.assertEquals(notification.getLegalFacts(), response);
  }

  @Test
  void givenNullLegalFactWhenGetLegalFactsThenDoNotThrow() {
    // Given
    String notificationId = "123";

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(notificationId);
    notification.setOrganizationId(1L);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(notificationId)).thenReturn(Optional.of(notification));

    // When
    Assertions.assertDoesNotThrow(() -> sendNotificationService.getLegalFacts(notificationId));
  }

  @Test
  void givenNoSendNotificationWhenDeleteExpiredLegalFactsThenThrowSendNotificationNotFoundException() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(SendNotificationNotFoundException.class, () ->
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken));
  }

  @Test
  void givenNullBrokerConfigurationWhenDeleteExpiredLegalFactsThenReturnNullNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(null);

    FileExpirationResponseDTO result =
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getNextFileExpirationDate());
  }

  @Test
  void givenBrokerConfigurationWithNullExpirationDaysWhenDeleteExpiredLegalFactsThenReturnNullNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(null);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getNextFileExpirationDate());
  }

  @Test
  void givenNoUpdateWhenDeleteExpiredLegalFactsThenThrowSendNotificationNotFoundException() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();
    OffsetDateTime downloadDate = OffsetDateTime.now().minusDays(60);
    String fileName = "expired.pdf";

    LegalFactDTO legalFact = LegalFactDTO.builder()
      .fileName(fileName)
      .url("some/url")
      .category(LegalFactCategoryDTO.SENDER_ACK)
      .status(FileStatus.READY)
      .downloadDate(downloadDate)
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(legalFact));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    UpdateResult failedUpdateResult = UpdateResult.acknowledged(1, 0L, null);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);
    Mockito.when(sendNotificationNoPIIRepositoryMock.updateLegalFactStatus(sendNotificationId, fileName, FileStatus.EXPIRED))
      .thenReturn(failedUpdateResult);

    Assertions.assertThrows(SendNotificationNotFoundException.class, () ->
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken));
  }

  @Test
  void givenLegalFactAlreadyExpiredWhenDeleteExpiredLegalFactsThenSkipItAndReturnNullNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    LegalFactDTO expiredLegalFact = LegalFactDTO.builder()
      .fileName("already-expired.pdf")
      .status(FileStatus.EXPIRED)
      .downloadDate(OffsetDateTime.now().minusDays(60))
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(expiredLegalFact));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getNextFileExpirationDate());
  }

  @Test
  void givenLegalFactWithNullDownloadDateWhenDeleteExpiredLegalFactsThenSkipItAndReturnNullNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    LegalFactDTO legalFactNoDownload = LegalFactDTO.builder()
      .fileName("no-download.pdf")
      .status(FileStatus.READY)
      .downloadDate(null)
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(legalFactNoDownload));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getNextFileExpirationDate());
  }

  @Test
  void givenNoExpiredLegalFactsWhenDeleteExpiredLegalFactsThenReturnNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    OffsetDateTime downloadDate1 = OffsetDateTime.now().minusDays(5);
    OffsetDateTime downloadDate2 = OffsetDateTime.now().minusDays(2);
    OffsetDateTime downloadDate3 = OffsetDateTime.now().minusDays(10);

    OffsetDateTime expectedNextFileExpirationDate = downloadDate2.plusDays(expirationDays);

    LegalFactDTO legalFact1 = LegalFactDTO.builder()
      .fileName("file1.pdf")
      .status(FileStatus.READY)
      .downloadDate(downloadDate1)
      .build();

    LegalFactDTO legalFact2 = LegalFactDTO.builder()
      .fileName("file2.pdf")
      .status(FileStatus.READY)
      .downloadDate(downloadDate2)
      .build();

    LegalFactDTO legalFact3 = LegalFactDTO.builder()
      .fileName("file3.pdf")
      .status(FileStatus.READY)
      .downloadDate(downloadDate3)
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(legalFact1, legalFact2, legalFact3));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedNextFileExpirationDate, result.getNextFileExpirationDate());
  }

  @Test
  void givenPartiallyExpiredLegalFactsWhenDeleteExpiredLegalFactsThenReturnMaxOfRemaining() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();

    OffsetDateTime recentDownloadDate = OffsetDateTime.now().minusDays(5);
    OffsetDateTime olderDownloadDate  = OffsetDateTime.now().minusDays(10);
    OffsetDateTime expiredDownloadDate = OffsetDateTime.now().minusDays(60);

    OffsetDateTime expectedNextFileExpirationDate = recentDownloadDate.plusDays(expirationDays);

    LegalFactDTO notExpiredFact1 = LegalFactDTO.builder()
      .fileName("not-expired-1.pdf")
      .status(FileStatus.READY)
      .downloadDate(recentDownloadDate)
      .build();

    LegalFactDTO notExpiredFact2 = LegalFactDTO.builder()
      .fileName("not-expired-2.pdf")
      .status(FileStatus.READY)
      .downloadDate(olderDownloadDate)
      .build();

    String expiredFileName = "expired.pdf";
    LegalFactDTO expiredFact = LegalFactDTO.builder()
      .fileName(expiredFileName)
      .status(FileStatus.READY)
      .downloadDate(expiredDownloadDate)
      .build();

    SendNotificationNoPII notification = podamFactory.manufacturePojo(SendNotificationNoPII.class);
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(notExpiredFact1, notExpiredFact2, expiredFact));

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);
    UpdateResult updateResult = UpdateResult.acknowledged(1, organizationId, null);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);
    Mockito.when(sendNotificationNoPIIRepositoryMock.updateLegalFactStatus(sendNotificationId, expiredFileName, FileStatus.EXPIRED))
      .thenReturn(updateResult);

    FileExpirationResponseDTO result =
      sendNotificationService.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedNextFileExpirationDate, result.getNextFileExpirationDate());
  }
}
