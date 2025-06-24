package it.gov.pagopa.pu.send.connector.pdnd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.pdnd.client.PdndApiClient;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdndServiceImplTest {

  @Mock
  private PdndApiClient pdndApiClient;

  @InjectMocks
  private PdndServiceImpl pdndService;

  private static final String ACCESS_TOKEN = "AccessToken";
  private PdndAuthData pdndAuthData;

  @BeforeEach
  public void setUp() {
    pdndAuthData = new PdndAuthData();
    pdndAuthData.setAccessToken(ACCESS_TOKEN);
  }

  @Test
  public void givenAccessTokenWhenResolvePdndAccessTokenThenIsValid() {
    // Given
    pdndAuthData.setExpiration(OffsetDateTime.now().plusHours(1));
    when(pdndApiClient.getVoucherToken(ACCESS_TOKEN)).thenReturn(pdndAuthData);

    // When
    String result = pdndService.resolvePdndAccessToken(ACCESS_TOKEN);

    // Then
    assertEquals(ACCESS_TOKEN, result);
    verify(pdndApiClient, times(1)).getVoucherToken(ACCESS_TOKEN);
    verify(pdndApiClient, never()).getVoucherToken(argThat(arg -> !arg.equals(ACCESS_TOKEN)));
  }

  @Test
  public void givenAccessTokenWhenResolvePdndAccessTokenThenIsExpired() {
    // Given
    pdndAuthData.setExpiration(OffsetDateTime.now().minusHours(1));
    when(pdndApiClient.getVoucherToken(ACCESS_TOKEN)).thenReturn(pdndAuthData);

    // When
    String result = pdndService.resolvePdndAccessToken(ACCESS_TOKEN);

    // Then
    assertEquals(ACCESS_TOKEN, result);
    verify(pdndApiClient, times(2)).getVoucherToken(ACCESS_TOKEN);
  }
}
