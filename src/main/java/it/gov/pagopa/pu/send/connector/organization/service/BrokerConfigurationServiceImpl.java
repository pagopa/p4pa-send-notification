package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.client.BrokerConfigurationSearchClient;
import org.springframework.stereotype.Service;

@Service
public class BrokerConfigurationServiceImpl implements BrokerConfigurationService {

  private final BrokerConfigurationSearchClient brokerConfigurationSearchClient;

  public BrokerConfigurationServiceImpl(BrokerConfigurationSearchClient brokerConfigurationSearchClient) {
    this.brokerConfigurationSearchClient = brokerConfigurationSearchClient;
  }

  @Override
  public BrokerConfiguration getBrokerConfigurationByOrganizationId(Long organizationId, String accessToken) {
    return brokerConfigurationSearchClient.getBrokerConfigurationByOrganizationId(organizationId,accessToken);
  }
}
