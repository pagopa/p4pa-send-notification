package it.gov.pagopa.pu.send.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
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
class SendFacadeServiceImplTest {
  @Mock
  private SendNotificationRepository sendNotificationRepository;

  @Mock
  private SendClient sendClient;

  @Mock
  private SendUploadFacadeServiceImpl uploadService;

  @Mock
  private SendNotification2NewNotificationRequestMapper sendNotificationMapper;

  @Mock
  private SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;

  @InjectMocks
  private SendFacadeServiceImpl sendService;


  @Test
  void givenValidNotificationWhenPreloadFilesThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    Long orgId = 1L;

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("digest123")
      .contentType("application/pdf")
      .status(FileStatus.READY)
      .build();
    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
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

    Mockito.when(sendClient.preloadFiles(List.of(preLoadRequestDTO), notification.getOrganizationId())).thenReturn(List.of(preLoadResponse));

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
    Long organizationId = 1L;

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
      .organizationId(organizationId)
      .status(NotificationStatus.REGISTERED)
      .documents(List.of(documentDTO))
      .build();

    Mockito.when(sendNotificationRepository.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));
    Mockito.when(uploadService.uploadFile(organizationId, sendNotificationId, documentDTO)).thenReturn(Optional.of(versionId));

    sendService.uploadFiles(sendNotificationId);
    Mockito.verify(sendNotificationRepository, Mockito.times(1)).updateFileVersionId(sendNotificationId, fileName, versionId);
    Mockito.verify(sendNotificationRepository, Mockito.times(1)).updateFileStatus(sendNotificationId, fileName, FileStatus.UPLOADED);
  }

  @Test
  void givenValidNotificationWhenDeliveryNotificationThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;

    NewNotificationResponseDTO response = new NewNotificationResponseDTO();
    response.setNotificationRequestId("NOTIFICATIONREQUESTID");

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .status(NotificationStatus.UPLOADED)
      .build();

    Mockito.when(sendNotificationRepository.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendClient.deliveryNotification(sendNotificationMapper.apply(notification), orgId)).thenReturn(response);

    sendService.deliveryNotification(sendNotificationId);

    Mockito.verify(sendNotificationRepository, Mockito.times(1))
      .updateNotificationRequestId(sendNotificationId, response.getNotificationRequestId());
    Mockito.verify(sendNotificationRepository, Mockito.times(1))
      .updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
  }

  @Test
  void givenValidNotificationWhenNotificationStatusThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "REQUESTID";
    Long orgId = 1L;

    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();
    response.setIun("IUN");

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .notificationRequestId(notificationRequestId)
      .status(NotificationStatus.COMPLETE)
      .build();

    Mockito.when(sendNotificationRepository.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendClient.notificationStatus(notificationRequestId, orgId)).thenReturn(response);

    NewNotificationRequestStatusResponseV24DTO result = sendService.notificationStatus(sendNotificationId);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(response, result);
    Mockito.verify(sendNotificationRepository, Mockito.times(1))
      .updateNotificationIun(sendNotificationId, response.getIun());
  }

  @Test
  void givenValidNotificationWhenRetrieveNotificationDataThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;
    String paxId = "PAXID";
    String noticeCode = "NOTICECODE";

    NotificationPriceResponseV23DTO response = new NotificationPriceResponseV23DTO();
    response.setNotificationViewDate(new Date());

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .payments(Collections.singletonList(new PuPayment(1L, new Payment(
        new PagoPa().noticeCode(noticeCode).creditorTaxId(paxId))))
      )
      .build();

    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    notificationDTO.setNotificationDate(OffsetDateTime.now());

    Mockito.when(sendNotificationRepository.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendClient.retrieveNotificationPrice(paxId, noticeCode, orgId)).thenReturn(response);
    Mockito.when(sendNotificationDTOMapper.apply(notification)).thenReturn(notificationDTO);

    SendNotificationDTO result = sendService.retrieveNotificationData(sendNotificationId);

    Assertions.assertNotNull(result);
  }

  @Test
  void givenValidNotificationWhenRetrieveNotificationDataNoContentThenNull() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;
    String paxId = "PAXID";
    String noticeCode = "NOTICECODE";

    NotificationPriceResponseV23DTO response = new NotificationPriceResponseV23DTO();

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .payments(Collections.singletonList(new PuPayment(1L, new Payment(
        new PagoPa().noticeCode(noticeCode).creditorTaxId(paxId))))
      )
      .build();

    Mockito.when(sendNotificationRepository.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendClient.retrieveNotificationPrice(paxId, noticeCode, orgId)).thenReturn(response);

    SendNotificationDTO result = sendService.retrieveNotificationData(sendNotificationId);

    Assertions.assertNull(result);
  }

  @Test
  void givenValidNotificationWhenRetrieveNotificationDataAlreadyExistsThenVerify() {
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;
    String paxId = "PAXID";
    String noticeCode = "NOTICECODE";

    SendNotification notification = SendNotification.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .payments(Collections.singletonList(new PuPayment(1L, new Payment(
        new PagoPa().noticeCode(noticeCode).creditorTaxId(paxId)))))
      .notificationData(OffsetDateTime.now())
      .build();

    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    notificationDTO.setNotificationDate(OffsetDateTime.now());

    Mockito.when(sendNotificationRepository.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendNotificationDTOMapper.apply(notification)).thenReturn(notificationDTO);

    SendNotificationDTO result = sendService.retrieveNotificationData(sendNotificationId);

    Assertions.assertNotNull(result);
  }

}
