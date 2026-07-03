package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.OrgSubUnit;
import it.gov.pagopa.pu.send.connector.organization.client.OrgSubUnitApiClient;
import org.springframework.stereotype.Service;

@Service
public class OrgSubUnitServiceImpl implements OrgSubUnitService {
  private final OrgSubUnitApiClient orgSubUnitApiClient;

  public OrgSubUnitServiceImpl(OrgSubUnitApiClient orgSubUnitApiClient) {
    this.orgSubUnitApiClient = orgSubUnitApiClient;
  }

  @Override
  public OrgSubUnit getOrgSubUnitById(Long organizationId, String orgSubUnitCode, String accessToken) {
    return orgSubUnitApiClient.getOrgSubUnitById(organizationId, orgSubUnitCode, accessToken);
  }
}
