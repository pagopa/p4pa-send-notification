package it.gov.pagopa.pu.send.connector.pdnd;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class PdndServiceImpl implements PdndService{
  private final PdndCacheService pdndCacheService;
  private final OrganizationService organizationService;

  public PdndServiceImpl(PdndCacheService pdndCacheService,
    OrganizationService organizationService) {
    this.pdndCacheService = pdndCacheService;
    this.organizationService = organizationService;
  }

  @Override
  public String resolvePdndAccessToken(Long organizationId, String accessToken) {
    Organization organization = organizationService.getOrganization(organizationId, accessToken);
    if(Boolean.FALSE.equals(organization.getPdndEnabled()))
      return null;

    PdndAuthData pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken, organizationId, null);
    if (pdndAuthData.getExpiration().isBefore(OffsetDateTime.now())) {
      pdndCacheService.evictPdndAccessToken(accessToken, organizationId, null);
      pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken, organizationId, null);
    }
    return pdndAuthData.getAccessToken();
  }
}
