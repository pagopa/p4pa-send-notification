package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.send.connector.organization.client.OrganizationApiClient;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.send.config.CacheConfig.Fields.organization)
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
  @Cacheable(key = "'orgId-' + #organizationId", unless = "#result == null")
  public Organization getOrganization(Long organizationId, String accessToken){
    return organizationApiClient.findByOrganizationId(organizationId, accessToken);
  }

  @Override
  @Cacheable(key = "'orgFiscalCode-' + #orgFiscalCode + '_segregationCode-' + #segregationCode", unless = "#result == null")
  public Optional<Organization> findByOrgFiscalCodeAndSegregationCode(String orgFiscalCode, String segregationCode, String accessToken) {
    return organizationApiClient.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken);
  }

  @Override
  @Cacheable(key = "'organizationId-' + #organizationId + '_stationId-' + #stationId", unless = "#result == null")
  public Optional<OrganizationStationDTO> findOrganizationStation(Long organizationId, String stationId, String accessToken) {
    return Optional.ofNullable(
      organizationApiClient.findOrganizationStation(organizationId, stationId, accessToken));
  }
}
