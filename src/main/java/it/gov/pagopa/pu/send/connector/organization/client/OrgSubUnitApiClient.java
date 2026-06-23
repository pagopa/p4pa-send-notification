package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.OrgSubUnit;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class OrgSubUnitApiClient {
  private final OrganizationApisHolder organizationApisHolder;

  public OrgSubUnitApiClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public OrgSubUnit getOrgSubUnitById(String orgSubUnitId, String accessToken) {
    try {
      return organizationApisHolder.getOrgSubUnitEntityControllerApi(accessToken)
        .crudGetOrgsubunit(orgSubUnitId);
    } catch (HttpClientErrorException.NotFound e) {
      log.warn("SubUnit with id {} not found", orgSubUnitId);
      return null;
    }
  }
}
