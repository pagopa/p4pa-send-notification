package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
        .getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization api key having organizationId {}", organizationId);
      return null;
    }
  }

  public Organization findByOrganizationId(Long organizationId, String accessToken) {
    try{
      return organizationApisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization(String.valueOf(organizationId));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having organizationId {}", organizationId);
      return null;
    }
  }

  public Optional<Organization> findByOrgFiscalCodeAndSegregationCode(String orgFiscalCode, String segregationCode, String accessToken) {
    try{
      return Optional.of(organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
        .crudOrganizationsFindByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having orgFiscalCode {} and segregationCode {}", orgFiscalCode, segregationCode);
      return Optional.empty();
    }
  }

}
