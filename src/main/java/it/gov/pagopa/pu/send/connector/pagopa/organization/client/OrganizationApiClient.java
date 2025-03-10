package it.gov.pagopa.pu.send.connector.pagopa.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.send.connector.pagopa.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class OrganizationApiClient {

  private final OrganizationApisHolder organizationApisHolder;

  public OrganizationApiClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public String getOrganizationApiKey(Long organizationId) {
    try{
      return organizationApisHolder.getOrganizationApi()
        .getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having organizationId {}", organizationId);
      return null;
    }
  }

}
