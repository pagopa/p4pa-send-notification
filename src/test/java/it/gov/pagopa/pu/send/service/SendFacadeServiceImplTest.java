package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.pagopa.send.SendService;
import it.gov.pagopa.pu.send.connector.pagopa.send.SendStreamService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO.EventTypeEnum;
import it.gov.pagopa.pu.send.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.SendLegalFactMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.mapper.SendStreamMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.repository.SendStreamRepository;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

import static it.gov.pagopa.pu.send.util.Constants.LEGAL_FACT_ID_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SendFacadeServiceImplTest {

  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private SendStreamRepository sendStreamRepositoryMock;
  @Mock
  private SendService sendServiceMock;
  @Mock
  private SendUploadFacadeServiceImpl uploadServiceMock;
  @Mock
  private SendNotification2NewNotificationRequestMapper sendNotificationMapperMock;
  @Mock
  private SendNotification2SendNotificationDTOMapper sendNotificationDTOMapperMock;
  @Mock
  private SendLegalFactMapper sendLegalFactMapperMock;
  @Mock
  private SendStreamMapper sendStreamMapperMock;
  @Mock
  private SendStreamService sendStreamServiceMock;
  @Mock
  private WorkflowService workflowService;
  @Mock
  private SendNotificationService sendNotificationServiceMock;
  @Mock
  private CloseableHttpClient httpClientMock;

  @InjectMocks
  private SendFacadeServiceImpl sendService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendNotificationNoPIIRepositoryMock,
      sendStreamRepositoryMock,
      sendServiceMock,
      uploadServiceMock,
      sendNotificationMapperMock,
      sendNotificationDTOMapperMock,
      sendLegalFactMapperMock,
      sendStreamMapperMock,
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

    assertEquals("[NOTIFICATION_NOT_FOUND] Notification not found with id: " + sendNotificationId, exception.getMessage());
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
  void givenValidNotificationWhenDeliveryNotificationThenVerifyAndCreateStream() {
    //GIVEN
    //DTO
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    UUID sendStreamId = UUID.randomUUID();
    Long orgId = 1L;
    String title = "SEND-STREAM_" + orgId;

    NewNotificationResponseDTO response = new NewNotificationResponseDTO();
    response.setNotificationRequestId("NOTIFICATIONREQUESTID");

    StreamCreationRequestV25DTO streamCreationRequestV25DTO = new StreamCreationRequestV25DTO();
    streamCreationRequestV25DTO.setTitle(title);
    streamCreationRequestV25DTO.setEventType(StreamCreationRequestV25DTO.EventTypeEnum.STATUS);

    StreamMetadataResponseV25DTO streamMetadataResponseV25DTO = new StreamMetadataResponseV25DTO();
    streamMetadataResponseV25DTO.setStreamId(sendStreamId);
    streamMetadataResponseV25DTO.setTitle(title);
    streamMetadataResponseV25DTO.setEventType(EventTypeEnum.STATUS);

    SendStream sendStream = new SendStream();
    sendStream.setStreamId(sendStreamId.toString());
    sendStream.setTitle(title);
    sendStream.setEventType(EventTypeEnum.STATUS.getValue());

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .status(NotificationStatus.UPLOADED)
      .build();
    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();

    //STUBS (in order of code execution)
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendStreamRepositoryMock.findByOrganizationId(orgId))
      .thenReturn(Collections.emptyList());
    Mockito.when(sendStreamServiceMock.createStream(streamCreationRequestV25DTO, orgId, accessToken))
      .thenReturn(streamMetadataResponseV25DTO);
    Mockito.when(
      sendStreamMapperMock.mapToSendStream(
        streamMetadataResponseV25DTO,
        orgId
      )
    ).thenReturn(sendStream);
    Mockito.when(sendStreamRepositoryMock.save(sendStream))
      .thenReturn(sendStream);
    Mockito.when(workflowService.sendNotificationStreamConsume(sendStreamId.toString(), accessToken))
      .thenReturn(WorkflowCreatedDTO.builder()
        .workflowId("wfId")
        .runId("runID")
        .build()
      );
    Mockito.when(sendNotificationMapperMock.apply(notification))
      .thenReturn(request);
    Mockito.when(sendServiceMock.deliveryNotification(request, orgId, accessToken)).thenReturn(response);

    //WHEN
    sendService.deliveryNotification(sendNotificationId, accessToken);

    //THEN
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationRequestId(sendNotificationId, response.getNotificationRequestId());
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
  }

  @Test
  void givenValidNotificationWhenDeliveryNotificationThenVerifyButNotCreateStream() {
    //GIVEN
    //DTO
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    UUID sendStreamId = UUID.randomUUID();
    Long orgId = 1L;
    String title = "SEND-STREAM_" + orgId;

    NewNotificationResponseDTO response = new NewNotificationResponseDTO();
    response.setNotificationRequestId("NOTIFICATIONREQUESTID");

    StreamCreationRequestV25DTO streamCreationRequestV25DTO = new StreamCreationRequestV25DTO();
    streamCreationRequestV25DTO.setTitle(title);
    streamCreationRequestV25DTO.setEventType(StreamCreationRequestV25DTO.EventTypeEnum.STATUS);

    StreamMetadataResponseV25DTO streamMetadataResponseV25DTO = new StreamMetadataResponseV25DTO();
    streamMetadataResponseV25DTO.setStreamId(sendStreamId);
    streamMetadataResponseV25DTO.setTitle(title);
    streamMetadataResponseV25DTO.setEventType(EventTypeEnum.STATUS);

    SendStream sendStream = new SendStream();
    sendStream.setStreamId(sendStreamId.toString());
    sendStream.setTitle(title);
    sendStream.setEventType(EventTypeEnum.STATUS.getValue());

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .status(NotificationStatus.UPLOADED)
      .build();
    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();

    //STUBS (in order of code execution)
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendStreamRepositoryMock.findByOrganizationId(orgId))
      .thenReturn(List.of(new SendStream()));
    Mockito.when(sendNotificationMapperMock.apply(notification))
      .thenReturn(request);
    Mockito.when(sendServiceMock.deliveryNotification(request, orgId, accessToken)).thenReturn(response);

    //WHEN
    sendService.deliveryNotification(sendNotificationId, accessToken);

    //THEN
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
  void givenValidNotificationErrorNonNullButEmptyWhenNotificationStatusThenVerify() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "REQUESTID";
    Long orgId = 1L;

    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();
    response.setIun("IUN");
    response.setErrors(new ArrayList<>());

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
  void givenValidNotificationInSendingWhenNotificationStatusThenVerify() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "REQUESTID";
    Long orgId = 1L;

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .notificationRequestId(notificationRequestId)
      .status(NotificationStatus.SENDING)
      .build();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    assertThrows(InvalidStatusException.class,
      () -> sendService.notificationStatus(sendNotificationId, accessToken));
  }

  @Test
  void givenValidNotificationWhenNotificationStatusThenErrors() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    String notificationRequestId = "REQUESTID";
    Long orgId = 1L;

    ProblemErrorDTO error = new ProblemErrorDTO();
    error.setCode("001");
    error.setDetail("ERROR");
    error.setElement("EL");

    UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();
    response.setIun("IUN");
    response.setErrors(List.of(error));

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .notificationRequestId(notificationRequestId)
      .status(NotificationStatus.COMPLETE)
      .build();

    SendNotificationDTO expectedResult = new SendNotificationDTO();
    expectedResult.setStatus(NotificationStatus.ERROR);

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendServiceMock.notificationStatus(notificationRequestId, orgId, accessToken)).thenReturn(response);

    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.same(notification)))
      .thenReturn(expectedResult);

    Mockito.when(sendNotificationNoPIIRepositoryMock.updateNotificationStatus(sendNotificationId, NotificationStatus.ERROR))
      .thenReturn(updateResult);

    SendNotificationDTO result = sendService.notificationStatus(sendNotificationId, accessToken);

    assertNotNull(result);
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationIun(sendNotificationId, response.getIun());
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationStatus(sendNotificationId, NotificationStatus.ERROR);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenValidNotificationWhenRetrieveNotificationDateThenVerify(boolean isPagoPaNull) {
    // Given
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;
    String creditorTaxId = "PAXID";
    String noticeCode = "NOTICECODE";

    OffsetDateTime viewDate = OffsetDateTime.now().minusDays(1);

    NotificationPriceResponseV23DTO response = new NotificationPriceResponseV23DTO();
    response.setNotificationViewDate(viewDate);

    Payment payment = new Payment(new PagoPa().creditorTaxId(creditorTaxId).noticeCode(noticeCode), null);
    if (isPagoPaNull) {
      payment = new Payment(null, F24Payment.builder().title("F24").build());
    }
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
    if (!isPagoPaNull) {
      Mockito.when(sendServiceMock.retrieveNotificationPrice(creditorTaxId, noticeCode, orgId, accessToken))
        .thenReturn(response);
    }
    Mockito.when(sendNotificationDTOMapperMock.apply(Mockito.any()))
      .thenReturn(expectedDTO);

    // When
    SendNotificationDTO result = sendService.retrieveNotificationDate(sendNotificationId, accessToken);

    // Then
    assertNotNull(result);
    assertEquals(expectedDTO, result);
    if (!isPagoPaNull) {
      Mockito.verify(sendNotificationNoPIIRepositoryMock)
        .updateNotificationDate(sendNotificationId, puPayment.getNotificationDate(), puPayment.getPayment().getPagoPa().getNoticeCode());
    }
    Mockito.verify(sendNotificationDTOMapperMock).apply(Mockito.any());
  }

  @Test
  void givenNotificationWithNullNotificationStatusWhenNotificationStatusThenReturnDTO() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .notificationRequestId("REQUESTID")
      .status(NotificationStatus.COMPLETE)
      .build();

    SendNotificationDTO expectedDTO = new SendNotificationDTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendServiceMock.notificationStatus("REQUESTID", orgId, accessToken))
      .thenReturn(null);

    Mockito.when(sendNotificationDTOMapperMock.apply(notification))
      .thenReturn(expectedDTO);

    SendNotificationDTO result = sendService.notificationStatus(sendNotificationId, accessToken);

    assertNotNull(result);
    assertEquals(expectedDTO, result);
    Mockito.verify(sendNotificationDTOMapperMock).apply(notification);
  }

  @Test
  void givenNotificationStatusWithNullErrorsWhenNotificationStatusThenReturnDTO() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long orgId = 1L;

    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();
    response.setIun("IUN");
    response.setErrors(null);

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .notificationRequestId("REQUESTID")
      .status(NotificationStatus.COMPLETE)
      .build();

    SendNotificationDTO expectedDTO = new SendNotificationDTO();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendServiceMock.notificationStatus("REQUESTID", orgId, accessToken))
      .thenReturn(response);

    Mockito.when(sendNotificationDTOMapperMock.apply(notification))
      .thenReturn(expectedDTO);

    SendNotificationDTO result = sendService.notificationStatus(sendNotificationId, accessToken);

    assertNotNull(result);
    assertEquals(expectedDTO, result);
    Mockito.verify(sendNotificationDTOMapperMock).apply(notification);
    Mockito.verify(sendNotificationNoPIIRepositoryMock).updateNotificationIun(sendNotificationId, "IUN");
  }


  @Test
  void givenValidOrganizationIdAndNavWhenRetrieveNotificationPriceThenSuccess() {
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String sendNotificationId = "SENDNOTIFICATIONID";
    String nav = "321";
    String creditorTaxId = "123456789";

    Payment payment = new Payment(new PagoPa().noticeCode(nav).creditorTaxId(creditorTaxId), null);
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
    String streamId = "streamId";
    String lastEventId = "lastEventId";
    Long organizationId = 1L;

    ProgressResponseElementV25DTO sendStreamEvent = new ProgressResponseElementV25DTO();
    List<ProgressResponseElementV25DTO> expectedEvents = List.of(sendStreamEvent);

    Mockito.when(sendStreamServiceMock.getStreamEvents(streamId, lastEventId, organizationId, accessToken))
      .thenReturn(expectedEvents);
    Mockito.when(sendStreamRepositoryMock.updateLastEventId(streamId, lastEventId))
      .thenReturn(UpdateResult.unacknowledged()); //only for stubbing, not used in getStreamEvents method

    List<ProgressResponseElementV25DTO> result = sendService.getStreamEvents(streamId, lastEventId, organizationId, accessToken);

    assertNotNull(result);
    assertEquals(expectedEvents, result);
  }

  @Test
  void givenEmptyStreamIdWhenGetStreamEventsThenFetchLastStreamAndReturnEvents() {
    String accessToken = "ACCESSTOKEN";
    String lastEventId = "lastEventId";
    UUID streamId = UUID.randomUUID();
    Long organizationId = 1L;

    StreamListElementDTO lastStream = new StreamListElementDTO();
    lastStream.setStreamId(streamId);

    List<StreamListElementDTO> streams = List.of(new StreamListElementDTO(), lastStream);
    ProgressResponseElementV25DTO sendStreamEvent = new ProgressResponseElementV25DTO();
    List<ProgressResponseElementV25DTO> expectedEvents = List.of(sendStreamEvent);

    Mockito.when(sendStreamServiceMock.getStreams(organizationId, accessToken)).thenReturn(streams);
    Mockito.when(sendStreamServiceMock.getStreamEvents(String.valueOf(streamId), lastEventId, organizationId, accessToken))
      .thenReturn(expectedEvents);
    Mockito.when(sendStreamRepositoryMock.updateLastEventId(String.valueOf(streamId), lastEventId))
      .thenReturn(UpdateResult.unacknowledged()); //only for stubbing, not used in getStreamEvents method

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

    assertEquals("[STREAMS_NOT_FOUND] Streams not found for this organization: " + organizationId, exception.getMessage());
  }

  @Test
  void givenValidParamsWhenGetStreamThenReturnSendStreamByOrganizationId() {
    //GIVEN
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String streamId = UUID.randomUUID().toString();
    SendStream sendStream = new SendStream();
    sendStream.setStreamId(streamId);
    sendStream.setOrganizationId(organizationId);
    SendStreamDTO expectedResponse = new SendStreamDTO();
    expectedResponse.setStreamId(streamId);
    expectedResponse.setOrganizationId(organizationId);
    StreamListElementDTO streamListElementDTO = new StreamListElementDTO();
    streamListElementDTO.setStreamId(UUID.fromString(streamId));

    Mockito.when(sendStreamRepositoryMock.findById(streamId))
      .thenReturn(Optional.of(sendStream));
    Mockito.when(sendStreamServiceMock.getStreams(organizationId, accessToken))
      .thenReturn(List.of(streamListElementDTO));
    Mockito.when(sendStreamMapperMock.mapToSendStreamDTO(sendStream)).thenReturn(expectedResponse);

    //WHEN
    SendStreamDTO actualResult = sendService.getStream(streamId, accessToken);

    //THEN
    assertNotNull(actualResult);
    assertEquals(expectedResponse, actualResult);
  }

  @Test
  void givenNotFoundStreamInCacheWhenGetStreamThenThrowNotFoundException() {
    //GIVEN
    String accessToken = "ACCESSTOKEN";
    String streamId = "streamId";

    Mockito.when(sendStreamRepositoryMock.findById(streamId))
      .thenReturn(Optional.empty());
    Mockito.doNothing()
      .when(sendStreamRepositoryMock)
      .deleteById(streamId);

    String expectedErrorMessage = "[STREAMS_NOT_FOUND] Send stream not found for streamId: streamId";

    //WHEN
    NotFoundException exception = assertThrows(
      NotFoundException.class,
      () -> sendService.getStream(streamId, accessToken)
    );

    //THEN
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void givenOldStreamInCacheWhenGetStreamThenThrowNotFoundException() {
    //GIVEN
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String streamId = "streamId";
    SendStream sendStream = new SendStream();
    sendStream.setOrganizationId(organizationId);

    Mockito.when(sendStreamRepositoryMock.findById(streamId))
      .thenReturn(Optional.of(sendStream));
    Mockito.when(sendStreamServiceMock.getStreams(organizationId, accessToken))
        .thenReturn(Collections.emptyList());
    Mockito.doNothing()
      .when(sendStreamRepositoryMock)
      .deleteById(streamId);

    String expectedErrorMessage = "[STREAMS_NOT_FOUND] Send stream not found for streamId: streamId";

    //WHEN
    NotFoundException exception = assertThrows(
      NotFoundException.class,
      () -> sendService.getStream(streamId, accessToken)
    );

    //THEN
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void givenInvalidNotificationWhenDeliveryNotificationThenError() {
    //GIVEN
    //DTO
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "SENDNOTIFICATIONID";
    UUID sendStreamId = UUID.randomUUID();
    Long orgId = 1L;
    String title = "SEND-STREAM_" + orgId;

    StreamCreationRequestV25DTO streamCreationRequestV25DTO = new StreamCreationRequestV25DTO();
    streamCreationRequestV25DTO.setTitle(title);
    streamCreationRequestV25DTO.setEventType(StreamCreationRequestV25DTO.EventTypeEnum.STATUS);

    StreamMetadataResponseV25DTO streamMetadataResponseV25DTO = new StreamMetadataResponseV25DTO();
    streamMetadataResponseV25DTO.setStreamId(sendStreamId);
    streamMetadataResponseV25DTO.setTitle(title);
    streamMetadataResponseV25DTO.setEventType(EventTypeEnum.STATUS);

    SendStream sendStream = new SendStream();
    sendStream.setStreamId(sendStreamId.toString());
    sendStream.setTitle(title);
    sendStream.setEventType(EventTypeEnum.STATUS.getValue());

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .organizationId(orgId)
      .status(NotificationStatus.UPLOADED)
      .build();

    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();

    //STUBS (in order of code execution)
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendStreamRepositoryMock.findByOrganizationId(orgId))
      .thenReturn(Collections.emptyList());
    Mockito.when(sendStreamServiceMock.createStream(streamCreationRequestV25DTO, orgId, accessToken))
      .thenReturn(streamMetadataResponseV25DTO);
    Mockito.when(
      sendStreamMapperMock.mapToSendStream(
        streamMetadataResponseV25DTO,
        orgId
      )
    ).thenReturn(sendStream);
    Mockito.when(sendStreamRepositoryMock.save(sendStream))
      .thenReturn(sendStream);
    Mockito.when(workflowService.sendNotificationStreamConsume(sendStreamId.toString(), accessToken))
      .thenReturn(WorkflowCreatedDTO.builder()
        .workflowId("wfId")
        .runId("runID")
        .build()
      );
    Mockito.when(sendNotificationMapperMock.apply(notification))
      .thenReturn(request);
    Mockito.when(sendServiceMock.deliveryNotification(request, orgId, accessToken))
      .thenThrow(HttpClientErrorException.Conflict.class);

    //WHEN
    Assertions.assertThrows(ResponseStatusException.class, () -> sendService.deliveryNotification(sendNotificationId, accessToken));

    //THEN
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.times(1))
      .updateNotificationStatus(sendNotificationId, NotificationStatus.ERROR);
    Mockito.verify(sendNotificationNoPIIRepositoryMock, Mockito.never())
      .updateNotificationRequestId(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenAcceptedNotificationWhenRetrieveLegalFactsThenSuccess() {
    // GIVEN
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    String iun = "1234";
    String taxId = "tax_id";

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .iun(iun)
      .organizationId(organizationId)
      .status(NotificationStatus.ACCEPTED)
      .build();

    //LegalFactId from SEND
    LegalFactsIdV20DTO legalFactsIdDTO = new LegalFactsIdV20DTO();
    legalFactsIdDTO.setKey("key");
    legalFactsIdDTO.setCategory("category");
    //LegalFact from SEND
    LegalFactListElementV20DTO legalFactListElementV20DTO = new LegalFactListElementV20DTO();
    legalFactListElementV20DTO.setIun(iun);
    legalFactListElementV20DTO.setTaxId(taxId);
    legalFactListElementV20DTO.setLegalFactsId(legalFactsIdDTO); //set id
    //Mock LegalFact list received from SEND
    List<LegalFactListElementV20DTO> mockResultList = Collections.singletonList(legalFactListElementV20DTO);

    //Mapped LegalFactId
    LegalFactIdDTO legalFactIdDTO = new LegalFactIdDTO();
    legalFactIdDTO.setKey("key");
    legalFactIdDTO.setCategory("category");
    //Mapped LegalFact
    LegalFactListElementDTO legalFactListElementDTO = new LegalFactListElementDTO();
    legalFactListElementDTO.setIun(iun);
    legalFactListElementDTO.setTaxId(taxId);
    legalFactListElementDTO.setLegalFactId(legalFactIdDTO); //set id
    //Expected mapped LegalFact list
    List<LegalFactListElementDTO> expectedResponse = Collections.singletonList(legalFactListElementDTO);

    Mockito.when(sendLegalFactMapperMock.mapLegalFactDTOFromSend(legalFactListElementV20DTO))
      .thenReturn(legalFactListElementDTO);
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendServiceMock.getLegalFacts(iun, organizationId, accessToken))
      .thenReturn(mockResultList);

    // WHEN
    List<LegalFactListElementDTO> result = sendService.retrieveLegalFacts(sendNotificationId, accessToken);

    // THEN
    assertEquals(expectedResponse, result);
  }

  @Test
  void givenNotFoundNotificationWhenRetrieveLegalFactsThenThrowSendNotificationNotFoundException() {
    //GIVEN
    String accessToken = "ACCESS_TOKEN";
    String sendNotificationId = "SEND_NOTIFICATION_ID";

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.empty());

    // WHEN
    SendNotificationNotFoundException exception = assertThrows(SendNotificationNotFoundException.class, () ->
      sendService.retrieveLegalFacts(sendNotificationId, accessToken)
    );

    // THEN
    assertEquals("[NOTIFICATION_NOT_FOUND] Notification not found with id: %s".formatted(sendNotificationId), exception.getMessage());
  }

  @Test
  void givenNotAcceptedNotificationWhenRetrieveLegalFactsThenThrowInvalidStatusException() {
    // GIVEN
    String accessToken = "ACCESS_TOKEN";
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    String iun = "1234";

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .iun(iun)
      .status(NotificationStatus.COMPLETE)
      .build();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    // WHEN
    InvalidStatusException exception = assertThrows(InvalidStatusException.class, () ->
      sendService.retrieveLegalFacts(sendNotificationId, accessToken)
    );

    // THEN
    assertEquals("[INVALID_NOTIFICATION_STATUS] Notification status error: Expected: %s, Actual: %s".formatted(NotificationStatus.ACCEPTED, NotificationStatus.COMPLETE), exception.getMessage());
  }

  @Test
  void givenAcceptedNotificationWhenRetrieveLegalFactDownloadMetadataThenSuccess() {
    // GIVEN
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    String iun = "1234";
    String legalFactId = "LEGAL_FACT_ID";
    String filename = "filename.pdf";
    String url = "http://URL";
    BigDecimal contentLength = new BigDecimal(1234);

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .iun(iun)
      .organizationId(organizationId)
      .status(NotificationStatus.ACCEPTED)
      .build();
    SendNotificationDTO notificationDTO = SendNotificationDTO.builder()
      .sendNotificationId(sendNotificationId)
      .iun(iun)
      .organizationId(organizationId)
      .status(NotificationStatus.ACCEPTED)
      .build();

    //Mock LegalFactDownloadMetadata received from SEND
    LegalFactDownloadMetadataResponseDTO mockedResponse = new LegalFactDownloadMetadataResponseDTO();
    mockedResponse.setFilename(filename);
    mockedResponse.setContentLength(contentLength);
    mockedResponse.setUrl(url);

    //Expected mapped LegalFactDownloadMetadata
    LegalFactDownloadMetadataDTO expectedResult = new LegalFactDownloadMetadataDTO();
    mockedResponse.setFilename(filename);
    mockedResponse.setContentLength(contentLength);
    mockedResponse.setUrl(url);

    Mockito.when(sendLegalFactMapperMock.mapLegalFactDownloadMetadataFromSend(mockedResponse))
      .thenReturn(expectedResult);
    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));
    Mockito.when(sendServiceMock.getLegalFactDownloadMetadata(iun, legalFactId, organizationId, accessToken))
      .thenReturn(mockedResponse);
    Mockito.when(sendNotificationDTOMapperMock.apply(notification))
      .thenReturn(notificationDTO);

    // WHEN
    LegalFactDownloadMetadataDTO actualResult = sendService.retrieveLegalFactDownloadMetadata(sendNotificationId, legalFactId, accessToken);

    // THEN
    assertEquals(expectedResult, actualResult);
  }

  @Test
  void givenNotFoundNotificationWhenRetrieveLegalFactDownloadMetadataThenThrowSendNotificationNotFoundException() {
    // GIVEN
    String accessToken = "ACCESS_TOKEN";
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    String legalFactId = "LEGAL_FACT_ID";

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.empty());

    // WHEN
    SendNotificationNotFoundException exception = assertThrows(SendNotificationNotFoundException.class, () ->
      sendService.retrieveLegalFactDownloadMetadata(sendNotificationId, legalFactId, accessToken)
    );

    // THEN
    assertEquals("[NOTIFICATION_NOT_FOUND] Notification not found with id: %s".formatted(sendNotificationId), exception.getMessage());
  }

  @Test
  void givenNotAcceptedNotificationWhenRetrieveLegalFactDownloadMetadataThenThrowInvalidStatusException() {
    // GIVEN
    String accessToken = "ACCESS_TOKEN";
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    String legalFactId = "LEGAL_FACT_ID";
    String iun = "1234";

    SendNotificationNoPII notification = SendNotificationNoPII.builder()
      .sendNotificationId(sendNotificationId)
      .iun(iun)
      .status(NotificationStatus.COMPLETE)
      .build();
    SendNotificationDTO notificationDTO = SendNotificationDTO.builder()
      .sendNotificationId(sendNotificationId)
      .iun(iun)
      .status(NotificationStatus.COMPLETE)
      .build();

    Mockito.when(sendNotificationNoPIIRepositoryMock.findById(sendNotificationId))
      .thenReturn(Optional.of(notification));

    Mockito.when(sendNotificationDTOMapperMock.apply(notification))
      .thenReturn(notificationDTO);

    // WHEN
    InvalidStatusException exception = assertThrows(InvalidStatusException.class, () ->
      sendService.retrieveLegalFactDownloadMetadata(sendNotificationId, legalFactId, accessToken)
    );

    // THEN
    assertEquals("[INVALID_NOTIFICATION_STATUS] Notification status error: Expected: %s, Actual: %s".formatted(NotificationStatus.ACCEPTED, NotificationStatus.COMPLETE), exception.getMessage());
  }

  @Test
  void givenNullSendNotificationDTOWhenDownloadAndArchiveSendLegalFactThenThrowSendNotificationNotFoundException() {
    //GIVEN
    String accessToken = "ACCESS_TOKEN";
    String notificationRequestId = "notificationRequestId";
    String sendNotificationId = "sendNotificationId";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.ANALOG_DELIVERY;
    String legalFactId = LEGAL_FACT_ID_PREFIX + "sendLegalFact.pdf";

    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setSendNotificationId(sendNotificationId);

    Mockito.when(sendNotificationServiceMock.findSendNotificationDTOByNotificationRequestId(notificationRequestId))
      .thenReturn(null);

    //WHEN
    SendNotificationNotFoundException sendNotificationNotFoundException = assertThrows(
      SendNotificationNotFoundException.class,
      () -> sendService.downloadAndArchiveSendLegalFact(
        notificationRequestId,
        category,
        legalFactId,
        accessToken
      )
    );

    //THEN
    Assertions.assertNotNull(sendNotificationNotFoundException);
    Assertions.assertEquals(
      "[NOTIFICATION_NOT_FOUND] Error in fetching SEND notification by notificationRequestId %s".formatted(notificationRequestId),
      sendNotificationNotFoundException.getMessage()
    );
  }

  @Test
  void givenNotificationInInvalidStatusWhenDownloadAndArchiveSendLegalFactThenThrowInvalidStatusException() {
    //GIVEN
    String accessToken = "accessToken";
    String notificationRequestId = "notificationRequestId";
    String sendNotificationId = "sendNotificationId";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.ANALOG_DELIVERY;
    String legalFactId = LEGAL_FACT_ID_PREFIX + "sendLegalFact.pdf";

    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setSendNotificationId(sendNotificationId);
    sendNotificationDTO.setStatus(NotificationStatus.COMPLETE);
    sendNotificationDTO.setIun("IUN");
    sendNotificationDTO.setOrganizationId(1L);

    Mockito.when(sendNotificationServiceMock.findSendNotificationDTOByNotificationRequestId(notificationRequestId))
      .thenReturn(sendNotificationDTO);

    Mockito.when(sendLegalFactMapperMock.polishLegalFactIdKey(legalFactId))
      .thenReturn("sendLegalFact.pdf");

    //WHEN
    InvalidStatusException invalidStatusException = assertThrows(
      InvalidStatusException.class,
      () -> sendService.downloadAndArchiveSendLegalFact(
        notificationRequestId,
        category,
        legalFactId,
        accessToken
      )
    );

    //THEN
    Assertions.assertNotNull(invalidStatusException);
    Assertions.assertEquals(
      "[INVALID_NOTIFICATION_STATUS] Notification status error: Expected: ACCEPTED, Actual: COMPLETE",
      invalidStatusException.getMessage()
    );
  }

  @Test
  void givenNullLegalFactDownloadMetadataDTOWhenDownloadAndArchiveSendLegalFactThenThrowNotFoundException() {
    //GIVEN
    String accessToken = "accessToken";
    String notificationRequestId = "notificationRequestId";
    String sendNotificationId = "sendNotificationId";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.ANALOG_DELIVERY;
    String polishedLegalFactId = "sendLegalFact.pdf";
    String legalFactId = LEGAL_FACT_ID_PREFIX + polishedLegalFactId;

    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setSendNotificationId(sendNotificationId);
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    sendNotificationDTO.setIun("IUN");
    sendNotificationDTO.setOrganizationId(1L);

    Mockito.when(sendNotificationServiceMock.findSendNotificationDTOByNotificationRequestId(notificationRequestId))
      .thenReturn(sendNotificationDTO);
    Mockito.when(sendLegalFactMapperMock.polishLegalFactIdKey(legalFactId))
      .thenReturn(polishedLegalFactId);
    Mockito.when(
      sendServiceMock.getLegalFactDownloadMetadata(
        "IUN",
        polishedLegalFactId,
        1L,
        accessToken
      )
    ).thenReturn(null);

    //WHEN
    NotFoundException notFoundException = assertThrows(
      NotFoundException.class,
      () -> sendService.downloadAndArchiveSendLegalFact(
        notificationRequestId,
        category,
        legalFactId,
        accessToken
      )
    );

    //THEN
    Assertions.assertNotNull(notFoundException);
    Assertions.assertEquals(
      "[LEGAL_FACT_URL_NOT_FOUND] Error in fetching SEND LegalFact pre-signed URL for sendNotificationDTO %s, category %s, legalFactId %s".formatted(sendNotificationId, category, polishedLegalFactId),
      notFoundException.getMessage()
    );
    Mockito.verify(sendLegalFactMapperMock)
      .mapLegalFactDownloadMetadataFromSend(null);
  }

  @Test
  void givenNullPreSignedUrlWhenDownloadAndArchiveSendLegalFactThenThrowNotFoundException() {
    //GIVEN
    String accessToken = "accessToken";
    String notificationRequestId = "notificationRequestId";
    String sendNotificationId = "sendNotificationId";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.ANALOG_DELIVERY;
    String legalFactId = LEGAL_FACT_ID_PREFIX + "sendLegalFact.pdf";

    LegalFactDownloadMetadataResponseDTO legalFactDownloadMetadataResponseDTO =
      new LegalFactDownloadMetadataResponseDTO();
    LegalFactDownloadMetadataDTO legalFactDownloadMetadataDTO = new LegalFactDownloadMetadataDTO();
    legalFactDownloadMetadataDTO.setUrl(null);

    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setSendNotificationId(sendNotificationId);
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    sendNotificationDTO.setIun("IUN");
    sendNotificationDTO.setOrganizationId(1L);

    Mockito.when(sendNotificationServiceMock.findSendNotificationDTOByNotificationRequestId(notificationRequestId))
      .thenReturn(sendNotificationDTO);

    Mockito.when(
      sendServiceMock.getLegalFactDownloadMetadata(
        "IUN",
        "sendLegalFact.pdf",
        1L,
        accessToken
      )
    ).thenReturn(legalFactDownloadMetadataResponseDTO);
    Mockito.when(sendLegalFactMapperMock.mapLegalFactDownloadMetadataFromSend(
        legalFactDownloadMetadataResponseDTO
      ))
      .thenReturn(legalFactDownloadMetadataDTO);

    Mockito.when(sendLegalFactMapperMock.polishLegalFactIdKey(legalFactId))
      .thenReturn("sendLegalFact.pdf");

    //WHEN
    NotFoundException notFoundException = assertThrows(
      NotFoundException.class,
      () -> sendService.downloadAndArchiveSendLegalFact(
        notificationRequestId,
        category,
        legalFactId,
        accessToken
      )
    );

    //THEN
    Assertions.assertNotNull(notFoundException);
    Assertions.assertEquals(
      "[LEGAL_FACT_URL_NOT_FOUND] Error in fetching SEND LegalFact pre-signed URL for sendNotificationDTO %s, category %s, legalFactId %s".formatted(sendNotificationId, category, "sendLegalFact.pdf"),
      notFoundException.getMessage()
    );
  }

  @Test
  void givenCorrectPreSignedUrlWhenDownloadAndArchiveSendLegalFactThenReturnOk() throws IOException {
    //GIVEN
    String accessToken = "accessToken";
    String notificationRequestId = "notificationRequestId";
    String sendNotificationId = "sendNotificationId";
    LegalFactCategoryDTO category = LegalFactCategoryDTO.ANALOG_DELIVERY;
    String legalFactId = LEGAL_FACT_ID_PREFIX + "sendLegalFact.pdf";

    LegalFactDownloadMetadataResponseDTO legalFactDownloadMetadataResponseDTO =
      new LegalFactDownloadMetadataResponseDTO();
    LegalFactDownloadMetadataDTO legalFactDownloadMetadataDTO = new LegalFactDownloadMetadataDTO();
    legalFactDownloadMetadataDTO.setUrl("http://localhost:8080");

    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setSendNotificationId(sendNotificationId);
    sendNotificationDTO.setStatus(NotificationStatus.ACCEPTED);
    sendNotificationDTO.setIun("IUN");
    sendNotificationDTO.setOrganizationId(1L);

    Mockito.when(sendNotificationServiceMock.findSendNotificationDTOByNotificationRequestId(notificationRequestId))
      .thenReturn(sendNotificationDTO);

    Mockito.when(
      sendServiceMock.getLegalFactDownloadMetadata(
        "IUN",
        "sendLegalFact.pdf",
        1L,
        accessToken
      )
    ).thenReturn(legalFactDownloadMetadataResponseDTO);
    Mockito.when(sendLegalFactMapperMock.mapLegalFactDownloadMetadataFromSend(
        legalFactDownloadMetadataResponseDTO
      ))
      .thenReturn(legalFactDownloadMetadataDTO);

    Mockito.when(sendLegalFactMapperMock.polishLegalFactIdKey(legalFactId))
      .thenReturn("sendLegalFact.pdf");

    byte[] testBytes = "test".getBytes();
    Mockito.when(
      httpClientMock.execute(
        Mockito.isA(HttpGet.class),
        Mockito.isA(HttpClientResponseHandler.class)
      )
    ).thenReturn(testBytes);

    ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);

    Mockito.doNothing().when(sendNotificationServiceMock).uploadSendLegalFact(
      Mockito.eq(sendNotificationId),
      Mockito.eq(category),
      Mockito.eq("sendLegalFact.pdf"),
      inputStreamArgumentCaptor.capture()
    );

    //WHEN
    sendService.downloadAndArchiveSendLegalFact(
      notificationRequestId,
      category,
      legalFactId,
      accessToken
    );

    //THEN
    Mockito.verify(sendNotificationServiceMock).uploadSendLegalFact(
      Mockito.eq(sendNotificationId),
      Mockito.eq(category),
      Mockito.eq("sendLegalFact.pdf"),
      inputStreamArgumentCaptor.capture()
    );
    Assertions.assertEquals(
      new String(new ByteArrayInputStream(testBytes).readAllBytes()),
      new String(inputStreamArgumentCaptor.getValue().readAllBytes())
    );

  }

}
