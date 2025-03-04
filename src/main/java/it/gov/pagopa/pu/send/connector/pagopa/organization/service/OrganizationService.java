package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

public interface OrganizationService {

  String getOrganizationApiKey(Long organizationId, String keyType, String accessToken);
}
