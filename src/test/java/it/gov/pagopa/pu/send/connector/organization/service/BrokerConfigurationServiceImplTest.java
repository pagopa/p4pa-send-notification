package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.client.BrokerConfigurationSearchClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrokerConfigurationServiceImplTest {

  @Mock
  private BrokerConfigurationSearchClient brokerConfigurationSearchClientMock;

  private BrokerConfigurationService brokerConfigurationService;

  @BeforeEach
  void init() {
    brokerConfigurationService = new BrokerConfigurationServiceImpl(brokerConfigurationSearchClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(brokerConfigurationSearchClientMock);
  }

  @Test
  void whenGetBrokerConfigurationByOrganizationIdThenInvokeClient() {
    Long organizationId = 1L;
    String accessToken = "accessToken";
    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();

    Mockito.when(brokerConfigurationSearchClientMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    BrokerConfiguration result = brokerConfigurationService.getBrokerConfigurationByOrganizationId(organizationId, accessToken);

    Assertions.assertSame(brokerConfiguration, result);
  }
}
