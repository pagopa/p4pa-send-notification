package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;

public interface BrokerService {

  Broker getBrokerByOrganizationId(Long organizationId, String accessToken);
}
