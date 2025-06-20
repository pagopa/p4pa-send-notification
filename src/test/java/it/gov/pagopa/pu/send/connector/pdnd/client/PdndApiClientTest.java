package it.gov.pagopa.pu.send.connector.pdnd.client;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.pdnd.client.generated.P4paPdndApi;
import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.pdnd.dto.generated.PdndServicesEnum;
import it.gov.pagopa.pu.send.connector.pdnd.config.PagopaPdndApisHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class PdndApiClientTest {

  @Mock
  private PagopaPdndApisHolder apisHolder;
  @Mock
  private P4paPdndApi p4paPdndApiMock;

  private PdndApiClient pdndApiClient;

  @BeforeEach
  void setUp() {
    pdndApiClient = new PdndApiClient(apisHolder);
  }

  @Test
  void givenValidRequestWhenGetP4paPdndApiByApiKeyThenVerifyResponse() {
    // Given
    String accessToken = "accessToken";
    PdndAuthData authData = new PdndAuthData();
    authData.setAccessToken(accessToken);

    Mockito.when(apisHolder.getP4paPdndApiByApiKey(accessToken))
      .thenReturn(p4paPdndApiMock);
    Mockito.when(p4paPdndApiMock.getVoucherToken(PdndServicesEnum.SEND))
      .thenReturn(authData);

    // When
    String result = pdndApiClient.getVoucherToken(accessToken);

    // Then
    assertSame(accessToken, result);
  }

  @Test
  void givenNotExistentServiceWhenGetP4paPdndApiByApiKeyThenReturnNull() {
    // Given
    String accessToken = "accessToken";

    Mockito.when(apisHolder.getP4paPdndApiByApiKey(accessToken))
      .thenReturn(p4paPdndApiMock);
    Mockito.when(p4paPdndApiMock.getVoucherToken(PdndServicesEnum.SEND))
      .thenThrow(
        HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    String result = pdndApiClient.getVoucherToken(accessToken);

    // Then
    assertNull(result);
  }
}
