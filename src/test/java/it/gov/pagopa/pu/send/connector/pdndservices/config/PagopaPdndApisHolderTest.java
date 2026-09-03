package it.gov.pagopa.pu.send.connector.pdndservices.config;

import it.gov.pagopa.pu.pdndservices.dto.generated.PdndServiceType;
import it.gov.pagopa.pu.send.config.json.JsonConfig;
import it.gov.pagopa.pu.send.connector.BaseApiHolderTest;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagopaPdndApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PagopaPdndApisHolder apisHolder;
  private PagopaPdndApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = PagopaPdndApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new PagopaPdndApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getP4paPdndApiByApiKey(null));
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
      token -> apisHolder.getP4paPdndApiByApiKey(token)
        .getVoucherToken(PdndServiceType.SEND, 1L, "subUnitCode"),
      new ParameterizedTypeReference<>() {}
    );
  }

  @Test
  void whenGetPagopaPdndApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      token -> apisHolder.getP4paPdndApiByApiKey(token)
        .getVoucherToken(PdndServiceType.SEND, 1L, "subUnitCode"),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload);
  }
}
