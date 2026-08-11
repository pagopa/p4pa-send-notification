package it.gov.pagopa.pu.send.connector.pdndservices;

import it.gov.pagopa.pu.pdndservices.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.config.CacheConfig.Fields;
import it.gov.pagopa.pu.send.connector.pdndservices.client.PdndApiClient;
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

  @Cacheable(key="#accessToken + '-' + #organizationId + '-' + #subUnitCode", unless = "#result == null")
  public PdndAuthData getPdndAccessToken(String accessToken, Long organizationId, String subUnitCode) {
    return pdndApiClient.getVoucherToken(accessToken, organizationId, subUnitCode);
  }

  @CacheEvict(key="#accessToken + '-' + #organizationId + '-' + #subUnitCode")
  public void evictPdndAccessToken(String accessToken, Long organizationId, String subUnitCode) {
    //empty body just trigger cache eviction!
  }
}
