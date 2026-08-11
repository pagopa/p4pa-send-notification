package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class OrganizationApiClient {

  private final OrganizationApisHolder organizationApisHolder;

  public OrganizationApiClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public String getOrganizationApiKey(Long organizationId, String accessToken) {
    try{
      return organizationApisHolder.getOrganizationApi(accessToken)
        .getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, null);
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find organization api key having organizationId {}", organizationId);
      return null;
    }
  }

  public Organization findByOrganizationId(Long organizationId, String accessToken) {
    try{
      return organizationApisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization(String.valueOf(organizationId));
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find organization having organizationId {}", organizationId);
      return null;
    }
  }

  public Optional<Organization> findByOrgFiscalCodeAndSegregationCode(String orgFiscalCode, String segregationCode, String accessToken) {
    try{
      return Optional.of(organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
        .crudOrganizationsFindByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode));
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find organization having orgFiscalCode {} and segregationCode {}", orgFiscalCode, segregationCode);
      return Optional.empty();
    }
  }

  public OrganizationStationDTO findOrganizationStation(Long organizationId, String stationId, String accessToken) {
    try{
      return organizationApisHolder.getOrganizationApi(accessToken)
        .getOrganizationStation(organizationId, stationId);
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find OrganizationStation having organizationId {} and StationId {}", organizationId, stationId);
      return null;
    }
  }

}
