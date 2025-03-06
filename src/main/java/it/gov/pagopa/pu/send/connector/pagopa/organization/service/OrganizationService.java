package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

import java.util.Optional;

public interface OrganizationService {

  String getOrganizationApiKey(Long organizationId, String keyType, String accessToken);

  Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken);
}
