package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdndservices.PdndService;
import it.gov.pagopa.send.dto.generated.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendServiceTest {

  @Mock
  private SendClient clientMock;
  @Mock
  private PdndService pdndServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private SendService service;

  private final String accessToken = "ACCESSTOKEN";
  private final String voucherToken = "VOUCHERTOKEN";

  @BeforeEach
  void init() {
    service = new SendServiceImpl(clientMock, organizationServiceMock, pdndServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(clientMock, organizationServiceMock, pdndServiceMock);
  }

  @Test
  void whenPreloadFilesThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    List<PreLoadRequestDTO> request = List.of();
    List<PreLoadResponseDTO> expectedResult = List.of();

    when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
        .thenReturn(orgSendApiKey);
    when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    when(clientMock.preloadFiles(Mockito.same(request), Mockito.same(orgSendApiKey), Mockito.same(voucherToken)))
      .thenReturn(expectedResult);

    // When
    List<PreLoadResponseDTO> result = service.preloadFiles(request, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenDeliveryNotificationThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    NewNotificationRequestV25DTO request = new NewNotificationRequestV25DTO();
    NewNotificationResponseDTO expectedResult = new NewNotificationResponseDTO();

    when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    when(clientMock.deliveryNotification(Mockito.same(request), Mockito.same(orgSendApiKey), Mockito.same(voucherToken)))
      .thenReturn(expectedResult);

    // When
    NewNotificationResponseDTO result = service.deliveryNotification(request, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenNotificationStatusThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    String notificationRequestId = "NOTIFICATION_ID";
    NewNotificationRequestStatusResponseV25DTO expectedResult = new NewNotificationRequestStatusResponseV25DTO();

    when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    when(clientMock.notificationStatus(Mockito.same(notificationRequestId), Mockito.same(orgSendApiKey), Mockito.same(voucherToken)))
      .thenReturn(expectedResult);

    // When
    NewNotificationRequestStatusResponseV25DTO result = service.notificationStatus(notificationRequestId, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenRetrieveNotificationPriceThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    String paTaxId = "PA_TAX_ID";
    String nav = "NAV";
    NotificationPriceResponseV23DTO expectedResult = new NotificationPriceResponseV23DTO();

    when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    when(clientMock.retrieveNotificationPrice(Mockito.same(paTaxId), Mockito.same(nav), Mockito.same(orgSendApiKey), Mockito.same(voucherToken)))
      .thenReturn(expectedResult);

    // When
    NotificationPriceResponseV23DTO result = service.retrieveNotificationPrice(paTaxId, nav, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenGetLegalFactsThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    List<LegalFactListElementV20DTO>  expectedResult = new ArrayList<>();

    when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    when(clientMock.getLegalFacts(Mockito.same(sendNotificationId), Mockito.same(orgSendApiKey), Mockito.same(voucherToken)))
      .thenReturn(expectedResult);

    // When
    List<LegalFactListElementV20DTO> result = service.getLegalFacts(sendNotificationId, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenGetLegalFactDownloadMetadataThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    String sendNotificationId = "SEND_NOTIFICATION_ID";
    String legalFactId = "LEGAL_FACT_ID";
    LegalFactDownloadMetadataResponseDTO expectedResult = new LegalFactDownloadMetadataResponseDTO();

    when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    when(clientMock.getLegalFactDownloadMetadata(Mockito.same(sendNotificationId), Mockito.same(legalFactId), Mockito.same(orgSendApiKey), Mockito.same(voucherToken)))
      .thenReturn(expectedResult);

    // When
    LegalFactDownloadMetadataResponseDTO actualResult = service.getLegalFactDownloadMetadata(sendNotificationId, legalFactId, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, actualResult);
  }

}
