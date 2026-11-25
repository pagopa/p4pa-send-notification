package it.gov.pagopa.pu.send.connector.pdndservices;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pdndservices.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.exception.common.NotFoundException;
import it.gov.pagopa.pu.send.util.Constants;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
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
  public String resolvePdndAccessToken(Long organizationId, String subUnitCode, String accessToken) {
    Organization organization = organizationService.getOrganization(organizationId, accessToken);
    if(Boolean.FALSE.equals(organization.getPdndEnabled()))
      return null;

    PdndAuthData pdndAuthData = getPdndAuthData(organizationId, subUnitCode, accessToken);
    if (pdndAuthData.getExpiration().isBefore(OffsetDateTime.now(Constants.ZONEID))) {
      pdndCacheService.evictPdndAccessToken(accessToken, organizationId, subUnitCode);
      pdndAuthData = getPdndAuthData(organizationId, subUnitCode, accessToken);
    }
    return pdndAuthData.getAccessToken();
  }

  private PdndAuthData getPdndAuthData(Long organizationId, String subUnitCode, String accessToken) {
    PdndAuthData pdndAuthData = pdndCacheService.getPdndAccessToken(accessToken, organizationId, subUnitCode);
    if (pdndAuthData == null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_PDND_VOUCHER_NOT_FOUND,
        String.format("PDND voucher for SEND not found for organizationId %s and subUnitCode %s", organizationId, subUnitCode));
    }
    return pdndAuthData;
  }
}
