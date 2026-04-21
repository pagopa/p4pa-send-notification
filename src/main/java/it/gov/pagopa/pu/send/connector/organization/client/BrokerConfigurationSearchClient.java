package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class BrokerConfigurationSearchClient {

  private final OrganizationApisHolder organizationApisHolder;

  public BrokerConfigurationSearchClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public BrokerConfiguration getBrokerConfigurationByOrganizationId(Long organizationId, String accessToken) {
    try {
      return organizationApisHolder.getBrokerConfigurationSearchControllerApi(accessToken)
        .crudBrokerConfigurationsFindByOrganizationId(organizationId);
    } catch (HttpClientErrorException.NotFound e) {
      log.warn("BrokerConfiguration for organization having organizationId {} not found", organizationId);
      return null;
    }
  }
}
