package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.send.connector.organization.client.BrokerSearchClient;
import org.springframework.stereotype.Service;

@Service
public class BrokerServiceImpl implements BrokerService {

  private final BrokerSearchClient brokerSearchClient;

  public BrokerServiceImpl(BrokerSearchClient brokerSearchClient) {
    this.brokerSearchClient = brokerSearchClient;
  }

  @Override
  public Broker getBrokerByOrganizationId(Long organizationId, String accessToken) {
    return brokerSearchClient.getBrokerByOrganizationId(organizationId, accessToken);
  }
}
