package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.pagopa.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.api.NewNotificationApi;
import it.gov.pagopa.pu.send.connector.send.generated.api.SenderReadB2BApi;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SendClientTest {

  @Mock
  private PagopaSendApisHolder apisHolder;
  @Mock
  private NewNotificationApi newNotificationApiMock;
  @Mock
  private SenderReadB2BApi senderReadB2BApiMock;
  @Mock
  private OrganizationService organizationService;

  private SendClient sendClient;
  private final String apiKey = "apiKey";
  private final String ipaCode = "ipaCode";
  private final String accessToken = "accessToken";
  private final String keyType = "SEND";
  private MockedStatic<SecurityUtils> securityUtilsMock;

  @BeforeEach
  void setUp() {
    securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
    securityUtilsMock.when(SecurityUtils::getOrganizationIpaCode).thenReturn(ipaCode);
    securityUtilsMock.when(SecurityUtils::getAccessToken).thenReturn(accessToken);

    sendClient = new SendClient(apisHolder, organizationService);
  }

  @AfterEach
  void tearDown() {
    securityUtilsMock.close();
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

    Mockito.when(organizationService.getOrganizationByIpaCode(ipaCode, accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(organizationService.getOrganizationApiKey(organizationId, keyType, accessToken))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNewNotificationApiByApiKey(apiKey))
      .thenReturn(newNotificationApiMock);
    Mockito.when(newNotificationApiMock.presignedUploadRequest(requestList))
      .thenReturn(responseList);

    // When
    List<PreLoadResponseDTO> result = sendClient.preloadFiles(requestList);

    // Then
    assertSame(responseList, result);
  }

  @Test
  void givenValidRequestWhenDeliveryNotificationThenVerifyResponse(){
    // Given
    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();
    NewNotificationResponseDTO response = new NewNotificationResponseDTO();

    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    Mockito.when(organizationService.getOrganizationByIpaCode(ipaCode, accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(organizationService.getOrganizationApiKey(organizationId, keyType, accessToken))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getNewNotificationApiByApiKey(apiKey))
      .thenReturn(newNotificationApiMock);
    Mockito.when(newNotificationApiMock.sendNewNotificationV24(request))
      .thenReturn(response);

    // When
    NewNotificationResponseDTO result = sendClient.deliveryNotification(request);

    // Then
    assertSame(response, result);
  }

  @Test
  void givenValidRequestWhenNotificationStatusThenVerifyResponse(){
    // Given
    String notificationRequestId = "REQUESTID";
    NewNotificationRequestStatusResponseV24DTO response = new NewNotificationRequestStatusResponseV24DTO();

    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    Mockito.when(organizationService.getOrganizationByIpaCode(ipaCode, accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(organizationService.getOrganizationApiKey(organizationId, keyType, accessToken))
      .thenReturn(apiKey);
    Mockito.when(apisHolder.getSenderReadB2BApiByApiKey(apiKey))
      .thenReturn(senderReadB2BApiMock);
    Mockito.when(senderReadB2BApiMock.retrieveNotificationRequestStatusV24(notificationRequestId, null, null))
      .thenReturn(response);

    // When
    NewNotificationRequestStatusResponseV24DTO result = sendClient.notificationStatus(notificationRequestId);

    // Then
    assertSame(response, result);
  }

  @Test
  void givenInvalidOrganizationWhenGetApiKeyThenThrowNotFoundException() {
    // Given
    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    securityUtilsMock.when(SecurityUtils::getOrganizationIpaCode).thenReturn(ipaCode);
    securityUtilsMock.when(SecurityUtils::getAccessToken).thenReturn(accessToken);

    Mockito.when(organizationService.getOrganizationByIpaCode(ipaCode, accessToken))
      .thenReturn(Optional.empty());

    // When
    NotFoundException exception = assertThrows(NotFoundException.class, () -> sendClient.notificationStatus("REQUESTID"));
    Assertions.assertEquals("Organization not found with ipaCode: " + ipaCode, exception.getMessage());
  }
}
