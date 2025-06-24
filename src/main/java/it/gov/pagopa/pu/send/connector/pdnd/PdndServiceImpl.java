package it.gov.pagopa.pu.send.connector.pdnd;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.pdnd.client.PdndApiClient;
import java.time.OffsetDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PdndServiceImpl implements PdndService{
  private final PdndApiClient pdndApiClient;

  public PdndServiceImpl(PdndApiClient pdndApiClient) {
    this.pdndApiClient = pdndApiClient;
  }

  @Cacheable(value="pdndVoucherTokens", key="#accessToken")
  private PdndAuthData getPdndAccessToken(String accessToken) {
    return pdndApiClient.getVoucherToken(accessToken);
  }

  @CacheEvict(value = "pdndVoucherTokens", key = "#accessToken")
  private void evictPdndAccessToken(String accessToken) {
  }

  @Override
  public String resolvePdndAccessToken(String accessToken) {
    PdndAuthData pdndAuthData = getPdndAccessToken(accessToken);
    if (pdndAuthData.getExpiration().isBefore(OffsetDateTime.now())) {
      evictPdndAccessToken(accessToken);
      pdndAuthData = getPdndAccessToken(accessToken);
    }
    return pdndAuthData.getAccessToken();
  }
}
