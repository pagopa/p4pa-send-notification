package it.gov.pagopa.pu.send.connector.pdnd.config;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndServicesEnum;
import it.gov.pagopa.pu.send.connector.BaseApiHolderTest;
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
class PagopaPdndApisHolderTest extends BaseApiHolderTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private PagopaPdndApisHolder apisHolder;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    PagopaPdndApiClientConfig apiClient = new PagopaPdndApiClientConfig();
    apiClient.setBaseUrl("http://example.com");
    apisHolder = new PagopaPdndApisHolder(apiClient, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetPagopaPdndApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      token -> apisHolder.getP4paPdndApiByApiKey(token)
        .getVoucherToken(PdndServicesEnum.SEND),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload);
  }
}
