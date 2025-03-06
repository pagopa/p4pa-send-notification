package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.pagopa.organization.client.OrganizationApiClient;
import it.gov.pagopa.pu.send.connector.pagopa.organization.client.OrganizationSearchClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationApiClient organizationApiClient;
  private final OrganizationSearchClient organizationSearchClient;

  public OrganizationServiceImpl(OrganizationApiClient organizationApiClient, OrganizationSearchClient organizationSearchClient) {
    this.organizationApiClient = organizationApiClient;
    this.organizationSearchClient = organizationSearchClient;
  }

  @Override
  public String getOrganizationApiKey(Long organizationId, String keyType, String accessToken) {
    return organizationApiClient.getOrganizationApiKey(organizationId, keyType, accessToken);
  }

  @Override
  public Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByIpaCode(ipaCode, accessToken)
    );
  }
}
