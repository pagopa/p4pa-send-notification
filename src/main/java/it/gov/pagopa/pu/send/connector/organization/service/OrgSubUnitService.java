package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.OrgSubUnit;

public interface OrgSubUnitService {
  OrgSubUnit getOrgSubUnitById(Long organizationId, String orgSubUnitCode, String accessToken);
}
