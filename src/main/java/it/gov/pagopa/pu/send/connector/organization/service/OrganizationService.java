package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface OrganizationService {

  String getOrganizationApiKey(Long organizationId, String accessToken);
  Organization getOrganization(Long organizationId, String accessToken);
}
