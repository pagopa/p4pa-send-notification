package it.gov.pagopa.pu.send.connector.pdnd;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.config.CacheConfig.Fields;
import it.gov.pagopa.pu.send.connector.pdnd.client.PdndApiClient;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = Fields.pdndAccessToken)
public class PdndCacheService {

  private final PdndApiClient pdndApiClient;

  public PdndCacheService(PdndApiClient pdndApiClient) {
    this.pdndApiClient = pdndApiClient;
  }

  @Cacheable(key="#accessToken", unless = "#result == null")
  public PdndAuthData getPdndAccessToken(String accessToken) {
    return pdndApiClient.getVoucherToken(accessToken);
  }

  @CacheEvict(key = "#accessToken")
  public void evictPdndAccessToken(String accessToken) {
    //empty body just trigger cache eviction!
  }
}
