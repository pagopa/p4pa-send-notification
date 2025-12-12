package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.connector.BaseApiHolderTest;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class PagopaSendApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PagopaSendApisHolder apisHolder;

  private final String voucherToken = "VOUCHERTOKEN";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    PagopaSendApiClientConfig apiClient = new PagopaSendApiClientConfig();
    apiClient.setBaseUrl("http://example.com");
    apisHolder = new PagopaSendApisHolder(apiClient, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetNewNotificationApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken)
        .sendNewNotificationV24(new NewNotificationRequestV24DTO()),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload,
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetSenderReadB2BApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getSenderReadB2BApiByApiKey(apiKey, voucherToken)
        .retrieveNotificationRequestStatusV24("REQUESTID",null, null),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload,
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetNotificationPriceApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getNotificationPriceApi(apiKey, voucherToken)
        .retrieveNotificationPriceV23("PATAXID","NOTICECODE"),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload,
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetStreamsApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getStreamsApi(apiKey, voucherToken)
        .listEventStreamsV25(),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload,
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetEventsApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getEventsApi(apiKey, voucherToken)
        .consumeEventStreamV25(UUID.randomUUID(),null),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload,
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetLegalFactsApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getLegalFactsApiByApiKey(apiKey, voucherToken)
        .retrieveNotificationLegalFactsV20("iun"),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload,
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }
}
