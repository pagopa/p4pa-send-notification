package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.SendService;
import it.gov.pagopa.pu.send.connector.pagopa.send.SendStreamService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO.EventTypeEnum;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SendFacadeServiceImplTest {

  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private SendService sendServiceMock;
  @Mock
  private SendUploadFacadeServiceImpl uploadServiceMock;
  @Mock
  private SendNotification2NewNotificationRequestMapper sendNotificationMapperMock;
  @Mock
  private SendNotification2SendNotificationDTOMapper sendNotificationDTOMapperMock;
  @Mock
  private SendStreamService sendStreamServiceMock;

  @InjectMocks
  private SendFacadeServiceImpl sendService;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      sendNotificationNoPIIRepositoryMock,
      sendServiceMock,
      uploadServiceMock,
      sendNotificationMapperMock,
      sendNotificationDTOMapperMock,
      sendStreamServiceMock
    );
  }

  @Test
  void givenValidNotificationWhenPreloadFilesThenVerify() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    String fileName = "FILENAME";
    Long orgId = 1L;

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName(fileName)
      .digest("digest123")
      .contentType("application/pdf")
      .status(FileStatus.READY)
      .build();
    SendNotificationNoPII notification = SendNotificationNoPII.builder()
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

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));

    Mockito.when(sendServiceMock.preloadFiles(List.of(preLoadRequestDTO), notification.getOrganizationId(), accessToken)).thenReturn(List.of(preLoadResponse));

    sendService.preloadFiles(sendNotificationId, accessToken);

    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1)).updateFilePreloadInformation(eq(sendNotificationId), any(PreLoadResponseDTO.class));
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1)).updateNotificationStatus(sendNotificationId, NotificationStatus.REGISTERED);
  }

  @Test
  void givenNotExistsNotificationWhenPreloadFilesThenException() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(SendNotificationNotFoundException.class, () -> sendService.preloadFiles(sendNotificationId, accessToken));

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

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(organizationId)
      .status(NotificationStatus.REGISTERED)
      .documents(List.of(documentDTO))
      .build();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId)).thenReturn(
      Optional.of(notification));
    Mockito.when(uploadServiceMock.uploadFile(organizationId, sendNotificationId, documentDTO)).thenReturn(Optional.of(versionId));

    sendService.uploadFiles(sendNotificationId);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateFileVersionId(sendNotificationId, fileName, versionId);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateFileStatus(sendNotificationId, fileName, FileStatus.UPLOADED);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateNotificationStatus(sendNotificationId, NotificationStatus.UPLOADED);
  }

  @Test
  void givenValidNotificationWhenDeliveryNotificationThenVerify() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;

    NewNotificationResponseDTO response = new NewNotificationResponseDTO();
    response.setNotificationRequestId("NOTIFICATIONREQUESTID");

    StreamCreationRequestV25DTO streamCreationRequestV25DTO = new StreamCreationRequestV25DTO();
    streamCreationRequestV25DTO.setTitle("SEND-STREAM_"+orgId);
    streamCreationRequestV25DTO.setEventType(StreamCreationRequestV25DTO.EventTypeEnum.STATUS);

    StreamMetadataResponseV25DTO streamMetadataResponseV25DTO = new StreamMetadataResponseV25DTO();
    streamMetadataResponseV25DTO.setTitle("SEND-STREAM_"+orgId);
    streamMetadataResponseV25DTO.setEventType(EventTypeEnum.STATUS);

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .status(NotificationStatus.UPLOADED)
      .build();
    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendNotificationMapperMock.apply(notification))
        .thenReturn(request);
    Mockito.when(sendStreamServiceMock.createStream(streamCreationRequestV25DTO, orgId, accessToken))
      .thenReturn(streamMetadataResponseV25DTO);

    Mockito.when(sendServiceMock.deliveryNotification(request, orgId, accessToken)).thenReturn(response);

    sendService.deliveryNotification(sendNotificationId, accessToken);

    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationRequestId(sendNotificationId, response.getNotificationRequestId());
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
  }

  @Test
  void givenValidNotificationWhenNotificationStatusThenVerify() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "REQUESTID";
    Long orgId = 1L;

    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();
    response.setIun("IUN");

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .notificationRequestId(notificationRequestId)
      .status(NotificationStatus.COMPLETE)
      .build();

    SendNotificationDTO expectedResult = new SendNotificationDTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendServiceMock.notificationStatus(notificationRequestId, orgId, accessToken)).thenReturn(response);

    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.same(notification)))
      .thenReturn(expectedResult);

    SendNotificationDTO result = sendService.notificationStatus(sendNotificationId, accessToken);

    assertNotNull(result);
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationIun(sendNotificationId, response.getIun());
  }

  @Test
  void givenValidNotificationWhenRetrieveNotificationDateThenVerify() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;
    String creditorTaxId = "PAXID";
    String noticeCode = "NOTICECODE";

    OffsetDateTime viewDate = OffsetDateTime.now().minusDays(1);

    NotificationPriceResponseV23DTO response = new NotificationPriceResponseV23DTO();
    response.setNotificationViewDate(viewDate);

    Payment payment = new Payment(new PagoPa().creditorTaxId(creditorTaxId).noticeCode(noticeCode));
    PuPayment puPayment = new PuPayment(1L, payment, null);
    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO(null, List.of(puPayment));

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .recipients(List.of(recipient))
      .build();

    SendNotificationDTO expectedDTO = new SendNotificationDTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendServiceMock.retrieveNotificationPrice(creditorTaxId, noticeCode, orgId, accessToken))
      .thenReturn(response);
    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.any()))
      .thenReturn(expectedDTO);

    // When
    SendNotificationDTO result = sendService.retrieveNotificationDate(sendNotificationId, accessToken);

    // Then
    assertNotNull(result);
    assertEquals(expectedDTO, result);
    Mockito.verify(sendNotificationNoPIIRepositoryMock)
      .updateNotificationDate(sendNotificationId, puPayment.getNotificationDate(), puPayment.getPayment().getPagoPa().getNoticeCode());
    Mockito.verify(sendNotificationDTOMapperMock).apply(Mockito.any());
  }

  @Test
  void givenValidOrganizationIdAndNavWhenRetrieveNotificationPriceThenSuccess() {
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String sendNotificationId = "SENDNOTIFICATIONID";
    String nav = "321";
    String creditorTaxId = "123456789";

    Payment payment = new Payment(new PagoPa().noticeCode(nav).creditorTaxId(creditorTaxId));
    PuPayment puPayment = new PuPayment(1L, payment, OffsetDateTime.now());
    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO(null, List.of(puPayment));

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(organizationId)
      .status(NotificationStatus.ACCEPTED)
      .recipients(List.of(recipient))
      .build();

    NotificationPriceResponseV23DTO expectedResponse = new NotificationPriceResponseV23DTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findByOrganizationIdAndNav(organizationId, nav))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendServiceMock.retrieveNotificationPrice(creditorTaxId, nav, organizationId, accessToken))
      .thenReturn(expectedResponse);

    NotificationPriceResponseV23DTO result = sendService.retrieveNotificationPrice(organizationId, nav, accessToken);

    assertEquals(expectedResponse, result);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).findByOrganizationIdAndNav(organizationId, nav);
    Mockito.verify(sendServiceMock).retrieveNotificationPrice(creditorTaxId, nav, organizationId, accessToken);
  }

  @Test
  void givenNotExistsOrganizationIdAndNavWhenRetrieveNotificationPriceThenNotFoundException() {
    String accessToken = "ACCESSTOKEN";
    // Given
    Long organizationId = 123L;
    String nav = "123456789";

    Mockito.when(sendNotificationNoPIIRepositoryMock.findByOrganizationIdAndNav(organizationId, nav))
      .thenReturn(Optional.empty());

    // When Then
    assertThrows(SendNotificationNotFoundException.class,
      () -> sendService.retrieveNotificationPrice(organizationId, nav, accessToken));
    Mockito.verify(sendNotificationNoPIIRepositoryMock).findByOrganizationIdAndNav(organizationId, nav);
    Mockito.verifyNoInteractions(sendServiceMock);
  }

  @Test
  void givenValidParamsWhenGetStreamEventsThenReturnEvents() {
    String accessToken = "ACCESSTOKEN";
    String streamId = "STREAMID";
    String lastEventId = "LASTEVENTID";
    Long organizationId = 1L;

    List<ProgressResponseElementV25DTO> expectedEvents = List.of(new ProgressResponseElementV25DTO());

    Mockito.when(sendStreamServiceMock.getStreamEvents(streamId, lastEventId, organizationId, accessToken))
      .thenReturn(expectedEvents);

    List<ProgressResponseElementV25DTO> result = sendService.getStreamEvents(streamId, lastEventId, organizationId, accessToken);

    assertNotNull(result);
    assertEquals(expectedEvents, result);
  }

  @Test
  void givenEmptyStreamIdWhenGetStreamEventsThenFetchLastStreamAndReturnEvents() {
    String accessToken = "ACCESSTOKEN";
    String lastEventId = "LASTEVENTID";
    UUID streamId = UUID.randomUUID();
    Long organizationId = 1L;

    StreamListElementDTO lastStream = new StreamListElementDTO();
    lastStream.setStreamId(streamId);

    List<StreamListElementDTO> streams = List.of(new StreamListElementDTO(), lastStream);
    List<ProgressResponseElementV25DTO> expectedEvents = List.of(new ProgressResponseElementV25DTO());

    Mockito.when(sendStreamServiceMock.getStreams(organizationId, accessToken)).thenReturn(streams);
    Mockito.when(sendStreamServiceMock.getStreamEvents(String.valueOf(streamId), lastEventId, organizationId, accessToken))
      .thenReturn(expectedEvents);

    List<ProgressResponseElementV25DTO> result = sendService.getStreamEvents(null, lastEventId, organizationId, accessToken);

    assertNotNull(result);
    assertEquals(expectedEvents, result);
  }

  @Test
  void givenEmptyStreamIdAndNoStreamsWhenGetStreamEventsThenThrowNotFoundException() {
    String accessToken = "ACCESSTOKEN";
    String lastEventId = "LASTEVENTID";
    Long organizationId = 1L;

    Mockito.when(sendStreamServiceMock.getStreams(organizationId, accessToken)).thenReturn(List.of());

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
      sendService.getStreamEvents(null, lastEventId, organizationId, accessToken)
    );

    assertEquals("Streams not found for this organization: " + organizationId, exception.getMessage());
  }




}
