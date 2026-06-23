package it.gov.pagopa.pu.send.connector.pdnd;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.pdnd.client.PdndApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(pdndApiClientMock);
  }

  @Test
  void whenGetPdndAccessTokenThenDelegatesToClient() {
    Long organizationId = 1L;
    String subUnitCode = "subUnitCode";

    when(pdndApiClientMock.getVoucherToken(ACCESS_TOKEN, organizationId, subUnitCode)).thenReturn(authData);

    PdndAuthData result = cacheService.getPdndAccessToken(ACCESS_TOKEN, organizationId, subUnitCode);

    assertEquals(ACCESS_TOKEN, result.getAccessToken());
  }

  @Test
  void whenEvictPdndAccessTokenThenNoError() {
    assertDoesNotThrow(() -> cacheService.evictPdndAccessToken(ACCESS_TOKEN, 1L, "subUnitCode"));
  }
}
