package it.gov.pagopa.pu.send.connector.pdnd;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PdndServiceImpl implements PdndService{
  private final PdndCacheService pdndCacheService;

  public PdndServiceImpl(PdndCacheService pdndCacheService) {
    this.pdndCacheService = pdndCacheService;
  }

  @Override
  public String resolvePdndAccessToken(String accessToken) {
    PdndAuthData pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken);
    log.info("PdndAuthData {}", pdndAuthData);
    if (pdndAuthData.getExpiration().isBefore(OffsetDateTime.now())) {
      pdndCacheService.evictPdndAccessToken(accessToken);
      pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken);
    }
    return pdndAuthData.getAccessToken();
  }
}
