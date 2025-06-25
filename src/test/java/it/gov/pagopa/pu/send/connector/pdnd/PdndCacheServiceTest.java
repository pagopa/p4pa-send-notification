package it.gov.pagopa.pu.send.connector.pdnd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.pdnd.client.PdndApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdndCacheServiceTest {

  @Mock
  private PdndApiClient pdndApiClientMock;

  private PdndCacheService cacheService;

  private static final String ACCESS_TOKEN = "TOKEN";
  private PdndAuthData authData;

  @BeforeEach
  void setup() {
    cacheService = new PdndCacheService(pdndApiClientMock);
    authData = new PdndAuthData();
    authData.setAccessToken(ACCESS_TOKEN);
  }

  @Test
  void whenGetPdndAccessTokenThenDelegatesToClient() {
    when(pdndApiClientMock.getVoucherToken(ACCESS_TOKEN)).thenReturn(authData);

    PdndAuthData result = cacheService.getPdndAccessToken(ACCESS_TOKEN);

    assertEquals(ACCESS_TOKEN, result.getAccessToken());
    verify(pdndApiClientMock, times(1)).getVoucherToken(ACCESS_TOKEN);
  }

  @Test
  void whenEvictPdndAccessTokenThenNoError() {
    assertDoesNotThrow(() -> cacheService.evictPdndAccessToken(ACCESS_TOKEN));
  }
}
