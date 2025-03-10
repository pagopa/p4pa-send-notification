package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

import it.gov.pagopa.pu.send.connector.pagopa.organization.client.OrganizationApiClient;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationApiClient organizationApiClient;

  public OrganizationServiceImpl(OrganizationApiClient organizationApiClient) {
    this.organizationApiClient = organizationApiClient;
  }

  @Override
  public String getOrganizationApiKey(Long organizationId) {
    return organizationApiClient.getOrganizationApiKey(organizationId);
  }
}
