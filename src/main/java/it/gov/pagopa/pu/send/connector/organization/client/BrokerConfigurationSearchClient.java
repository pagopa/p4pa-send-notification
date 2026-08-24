package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    } catch (RestInvokeNotFoundException e) {
      log.warn("BrokerConfiguration for organization having organizationId {} not found", organizationId);
      return null;
    }
  }
}
