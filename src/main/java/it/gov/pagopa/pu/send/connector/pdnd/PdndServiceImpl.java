package it.gov.pagopa.pu.send.connector.pdnd;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class PdndServiceImpl implements PdndService{
  private final PdndCacheService pdndCacheService;

  public PdndServiceImpl(PdndCacheService pdndCacheService) {
    this.pdndCacheService = pdndCacheService;
  }

  @Override
  public String resolvePdndAccessToken(String accessToken) {
    PdndAuthData pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken);
    if (pdndAuthData.getExpiration().isBefore(OffsetDateTime.now())) {
      pdndCacheService.evictPdndAccessToken(accessToken);
      pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken);
    }
    return pdndAuthData.getAccessToken();
  }
}
