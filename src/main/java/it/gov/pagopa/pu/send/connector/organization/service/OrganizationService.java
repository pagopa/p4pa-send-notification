package it.gov.pagopa.pu.send.connector.organization.service;

public interface OrganizationService {

  String getOrganizationApiKey(Long organizationId, String accessToken);
}
