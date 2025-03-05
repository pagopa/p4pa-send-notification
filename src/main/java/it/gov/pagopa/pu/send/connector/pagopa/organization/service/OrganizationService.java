package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

public interface OrganizationService {

  String getOrganizationApiKey(String organizationId, String keyType, String accessToken);
}
