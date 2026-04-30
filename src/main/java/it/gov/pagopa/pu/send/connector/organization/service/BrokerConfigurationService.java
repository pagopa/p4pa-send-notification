package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;

public interface BrokerConfigurationService {
  BrokerConfiguration getBrokerConfigurationByOrganizationId(Long organizationId, String accessToken);
}
