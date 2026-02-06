package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.api.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class SendClientTest {

  @Mock
  private PagopaSendApisHolder apisHolder;
  @Mock
  private NewNotificationApi newNotificationApiMock;
  @Mock
  private SenderReadB2BApi senderReadB2BApiMock;
  @Mock
  private NotificationPriceV23Api notificationPriceApiMock;
  @Mock
  private StreamsApi streamsApiMock;
  @Mock
  private EventsApi eventsApiMock;
  @Mock
  private LegalFactsApi legalFactsApiMock;

  private SendClient sendClient;
  private final String apiKey = "apiKey";
  private final String voucherToken = "voucherToken";

  @BeforeEach
  void setUp() {
    sendClient = new SendClient(apisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      apisHolder,
      newNotificationApiMock,
      senderReadB2BApiMock,
      notificationPriceApiMock,
      streamsApiMock,
      eventsApiMock,
      legalFactsApiMock
    );
  }

  @Test
  void givenValidRequestWhenPreloadFilesThenVerifyResponse() {
    // Given
    PreLoadRequestDTO requestDTO = new PreLoadRequestDTO();
    requestDTO.setPreloadIdx("TEST");
    requestDTO.setContentType("application/pdf");
    requestDTO.setSha256("asdsadasdoaisdasldk");
    List<PreLoadRequestDTO> requestList = List.of(requestDTO);

    PreLoadResponseDTO responseDTO = new PreLoadResponseDTO();
    responseDTO.setPreloadIdx("TEST");
    responseDTO.setKey("mock-key");
    responseDTO.setSecret("mock-secret");
    responseDTO.setHttpMethod(HttpMethodEnum.PUT);
    responseDTO.setUrl("https://mock-url.com");
    List<PreLoadResponseDTO> responseList = List.of(responseDTO);

    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    Mockito.when(apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken))
      .thenReturn(newNotificationApiMock);
    Mockito.when(newNotificationApiMock.presignedUploadRequest(requestList))
      .thenReturn(responseList);

    // When
    List<PreLoadResponseDTO> result = sendClient.preloadFiles(requestList, apiKey, voucherToken);

    // Then
    assertSame(responseList, result);
  }

  @Test
  void givenValidRequestWhenDeliveryNotificationThenVerifyResponse(){
    // Given
    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();
    NewNotificationResponseDTO response = new NewNotificationResponseDTO();

    Mockito.when(apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken))
      .thenReturn(newNotificationApiMock);
    Mockito.when(newNotificationApiMock.sendNewNotificationV24(request))
      .thenReturn(response);

    // When
    NewNotificationResponseDTO result = sendClient.deliveryNotification(request, apiKey, voucherToken);

    // Then
    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenNotificationStatusThenVerifyResponse(){
    // Given
    String notificationRequestId = "REQUESTID";
    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();

    Mockito.when(apisHolder.getSenderReadB2BApiByApiKey(apiKey, voucherToken))
      .thenReturn(senderReadB2BApiMock);
    Mockito.when(senderReadB2BApiMock.retrieveNotificationRequestStatusV24(notificationRequestId, null, null))
      .thenReturn(response);

    // When
    NewNotificationRequestStatusResponseV24DTO result = sendClient.notificationStatus(notificationRequestId, apiKey, voucherToken);

    // Then
    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenRetrieveNotificationPriceThenVerifyResponse() {
    String paTaxId = "TAXID";
    String noticeCode = "NOTICECODE";

    NotificationPriceResponseV23DTO response = new NotificationPriceResponseV23DTO();

    Mockito.when(apisHolder.getNotificationPriceApi(apiKey, voucherToken))
      .thenReturn(notificationPriceApiMock);
    Mockito.when(notificationPriceApiMock.retrieveNotificationPriceV23(paTaxId, noticeCode))
      .thenReturn(response);

    NotificationPriceResponseV23DTO result = sendClient.retrieveNotificationPrice(paTaxId, noticeCode, apiKey, voucherToken);

    assertSame(response, result);
  }

  @Test
  void givenNewStreamWhenCreateStreamThenVerifyResponse() {

    StreamCreationRequestV25DTO request = new StreamCreationRequestV25DTO();
    StreamMetadataResponseV25DTO response = new StreamMetadataResponseV25DTO();

    Mockito.when(apisHolder.getStreamsApi(apiKey, voucherToken))
      .thenReturn(streamsApiMock);
    Mockito.when(streamsApiMock.createEventStreamV25(request))
      .thenReturn(response);

    StreamMetadataResponseV25DTO result = sendClient.createStream(request, apiKey, voucherToken);

    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenGetStreamsThenVerifyResponse() {
    List<StreamListElementDTO> response = List.of();

    Mockito.when(apisHolder.getStreamsApi(apiKey, voucherToken))
      .thenReturn(streamsApiMock);
    Mockito.when(streamsApiMock.listEventStreamsV25())
      .thenReturn(response);

    List<StreamListElementDTO> result = sendClient.getStreams(apiKey, voucherToken);

    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenGetStreamEventsThenVerifyResponse() {
    UUID streamId = UUID.randomUUID();
    List<ProgressResponseElementV25DTO> response = List.of();

    Mockito.when(apisHolder.getEventsApi(apiKey, voucherToken))
      .thenReturn(eventsApiMock);
    Mockito.when(eventsApiMock.consumeEventStreamV25(streamId, null))
      .thenReturn(response);

    List<ProgressResponseElementV25DTO> result = sendClient.getStreamEvents(
      String.valueOf(streamId), null, apiKey, voucherToken);

    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenGetLegalFactsThenVerifyResponse() {
    // GIVEN
    String iun = "REQUEST_ID";

    //LegalFactId from SEND
    LegalFactsIdV20DTO legalFactsIdDTO = new LegalFactsIdV20DTO();
    legalFactsIdDTO.setKey("key");
    legalFactsIdDTO.setCategory("category");
    //LegalFact from SEND
    LegalFactListElementV20DTO legalFactListElementV20DTO = new LegalFactListElementV20DTO();
    legalFactListElementV20DTO.setIun("iun");
    legalFactListElementV20DTO.setTaxId("taxId");
    legalFactListElementV20DTO.setLegalFactsId(legalFactsIdDTO); //set id
    //Expected LegalFact list received from SEND
    List<LegalFactListElementV20DTO> expectedResult = Collections.singletonList(legalFactListElementV20DTO);

    Mockito.when(apisHolder.getLegalFactsApiByApiKey(apiKey, voucherToken))
      .thenReturn(legalFactsApiMock);
    Mockito.when(legalFactsApiMock.retrieveNotificationLegalFactsV20(iun))
      .thenReturn(expectedResult);

    // WHEN
    List<LegalFactListElementV20DTO> result = sendClient.getLegalFacts(iun, apiKey, voucherToken);

    // THEN
    assertSame(expectedResult, result);
  }

  @Test
  void givenValidRequestWhenGetLegalFactDownloadMetadataThenVerifyResponse() {
    // GIVEN
    String iun = "NOTIFICATION_ID";
    String legalFactId = "LEGAL_FACT_ID";
    String filename = "filename.pdf";
    String url = "http://URL";
    BigDecimal contentLength = new BigDecimal(1234);

    //Mock LegalFactDownloadMetadata received from SEND
    LegalFactDownloadMetadataResponseDTO expectedResult = new LegalFactDownloadMetadataResponseDTO();
    expectedResult.setFilename(filename);
    expectedResult.setContentLength(contentLength);
    expectedResult.setUrl(url);

    Mockito.when(apisHolder.getLegalFactsApiByApiKey(apiKey, voucherToken))
      .thenReturn(legalFactsApiMock);
    Mockito.when(legalFactsApiMock.downloadLegalFactById(iun, legalFactId))
      .thenReturn(expectedResult);

    // WHEN
    LegalFactDownloadMetadataResponseDTO actualResult = sendClient.getLegalFactDownloadMetadata(iun,legalFactId, apiKey, voucherToken);

    // THEN
    assertSame(expectedResult, actualResult);
  }

}
