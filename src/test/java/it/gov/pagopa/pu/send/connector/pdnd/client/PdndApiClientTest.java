package it.gov.pagopa.pu.send.connector.pdnd.client;

import it.gov.pagopa.pu.pdnd.client.generated.P4paPdndApi;
import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.pdnd.dto.generated.PdndServiceType;
import it.gov.pagopa.pu.send.connector.pdnd.config.PagopaPdndApisHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
    Long organizationId = 1L;
    String subUnitCode = "subUnitCode";

    Mockito.when(apisHolder.getP4paPdndApiByApiKey(accessToken))
      .thenReturn(p4paPdndApiMock);
    Mockito.when(p4paPdndApiMock.getVoucherToken(PdndServiceType.SEND, organizationId, subUnitCode))
      .thenReturn(authData);

    // When
    PdndAuthData result = pdndApiClient.getVoucherToken(accessToken, organizationId, subUnitCode);

    // Then
    assertSame(accessToken, result.getAccessToken());
  }

  @Test
  void givenNotExistentServiceWhenGetP4paPdndApiByApiKeyThenReturnNull() {
    // Given
    String accessToken = "accessToken";
    Long organizationId = 1L;
    String subUnitCode = "subUnitCode";

    Mockito.when(apisHolder.getP4paPdndApiByApiKey(accessToken))
      .thenReturn(p4paPdndApiMock);
    Mockito.when(p4paPdndApiMock.getVoucherToken(PdndServiceType.SEND, organizationId, subUnitCode))
      .thenThrow(
        HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    PdndAuthData result = pdndApiClient.getVoucherToken(accessToken, organizationId, subUnitCode);

    // Then
    assertNull(result);
  }
}
