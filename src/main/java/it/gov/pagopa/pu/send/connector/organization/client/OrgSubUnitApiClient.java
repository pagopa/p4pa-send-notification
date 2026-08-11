package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.OrgSubUnit;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrgSubUnitApiClient {
  private final OrganizationApisHolder organizationApisHolder;

  public OrgSubUnitApiClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public OrgSubUnit getOrgSubUnitById(Long organizationId, String orgSubUnitCode, String accessToken) {
   String orgSubUnitId = calculateOrgSubUnitId(organizationId, orgSubUnitCode);

    try {
      return organizationApisHolder.getOrgSubUnitEntityControllerApi(accessToken)
        .crudGetOrgsubunit(orgSubUnitId);
    } catch (RestInvokeNotFoundException e) {
      log.warn("SubUnit with id {} not found", orgSubUnitId);
      return null;
    }
  }

  private String calculateOrgSubUnitId(Long organizationId, String subUnitCode) {
    return organizationId + "-" + subUnitCode;
  }
}
