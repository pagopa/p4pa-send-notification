package it.gov.pagopa.pu.send.connector.pdnd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdndServiceImplTest {

  @Mock
  private PdndCacheService pdndCacheServiceMock;

  private PdndServiceImpl pdndService;

  private static final String ACCESS_TOKEN = "AccessToken";
  private PdndAuthData pdndAuthData;

  @BeforeEach
  void setUp() {
    pdndAuthData = new PdndAuthData();
    pdndAuthData.setAccessToken(ACCESS_TOKEN);

    pdndService = new PdndServiceImpl(pdndCacheServiceMock);
  }

  @Test
  void givenAccessTokenWhenResolvePdndAccessTokenThenIsValid() {
    // Given
    pdndAuthData.setExpiration(OffsetDateTime.now().plusHours(1));
    when(pdndCacheServiceMock.getPdndAccessToken(ACCESS_TOKEN)).thenReturn(pdndAuthData);

    // When
    String result = pdndService.resolvePdndAccessToken(ACCESS_TOKEN);

    // Then
    assertEquals(ACCESS_TOKEN, result);
    verify(pdndCacheServiceMock, times(1)).getPdndAccessToken(ACCESS_TOKEN);
    verify(pdndCacheServiceMock, never()).getPdndAccessToken(argThat(arg -> !arg.equals(ACCESS_TOKEN)));
  }

  @Test
  void givenAccessTokenWhenResolvePdndAccessTokenThenIsExpired() {
    // Given
    pdndAuthData.setExpiration(OffsetDateTime.now().minusHours(1));
    when(pdndCacheServiceMock.getPdndAccessToken(ACCESS_TOKEN)).thenReturn(pdndAuthData);

    // When
    String result = pdndService.resolvePdndAccessToken(ACCESS_TOKEN);

    // Then
    assertEquals(ACCESS_TOKEN, result);
    verify(pdndCacheServiceMock, times(2)).getPdndAccessToken(ACCESS_TOKEN);
  }
}
