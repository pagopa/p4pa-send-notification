package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.organization.client.OrganizationApiClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationApiClient organizationApiClient;

  public OrganizationServiceImpl(OrganizationApiClient organizationApiClient) {
    this.organizationApiClient = organizationApiClient;
  }

  @Override
  public String getOrganizationApiKey(Long organizationId, String accessToken) {
    return organizationApiClient.getOrganizationApiKey(organizationId, accessToken);
  }

  @Override
  public Organization getOrganization(Long organizationId, String accessToken){
    return organizationApiClient.findByOrganizationId(organizationId, accessToken);
  }

  @Override
  public Optional<Organization> findByOrgFiscalCodeAndSegregationCode(String orgFiscalCode, String segregationCode, String accessToken) {
    return organizationApiClient.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken);
  }
}
