package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.config.json.JsonConfig;
import it.gov.pagopa.pu.send.connector.BaseApiHolderTest;
import it.gov.pagopa.send.dto.generated.NewNotificationRequestV24DTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagopaSendApisHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PagopaSendApisHolder apisHolder;
  private PagopaSendApiClientConfig apiClientConfig;

  private final String voucherToken = "VOUCHERTOKEN";

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = PagopaSendApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new PagopaSendApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getNewNotificationApiByApiKey(null, voucherToken));
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      apiKey -> apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken)
        .sendNewNotificationV24(new NewNotificationRequestV24DTO()),
      new ParameterizedTypeReference<>() {}
    );
  }

  @Test
  void whenGetNewNotificationApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken)
        .sendNewNotificationV24(new NewNotificationRequestV24DTO()),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetSenderReadB2BApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getSenderReadB2BApiByApiKey(apiKey, voucherToken)
        .retrieveNotificationRequestStatusV24("REQUESTID",null, null),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetNotificationPriceApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getNotificationPriceApi(apiKey, voucherToken)
        .retrieveNotificationPriceV23("PATAXID","NOTICECODE"),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetStreamsApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getStreamsApi(apiKey, voucherToken)
        .listEventStreamsV25(),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetEventsApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getEventsApi(apiKey, voucherToken)
        .consumeEventStreamV25(UUID.randomUUID(),null),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }

  @Test
  void whenGetLegalFactsApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      apiKey -> apisHolder.getLegalFactsApiByApiKey(apiKey, voucherToken)
        .retrieveNotificationLegalFactsV20("iun"),
      new ParameterizedTypeReference<>() {},
      () -> {},
      AUTH_TYPE.API_KEY,
      "x-api-key");
  }
}
