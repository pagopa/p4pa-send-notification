package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.send.connector.organization.client.BrokerSearchClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrokerServiceImplTest {

  @Mock
  private BrokerSearchClient brokerSearchClientMock;

  private BrokerService brokerService;

  @BeforeEach
  void init() {
    brokerService = new BrokerServiceImpl(brokerSearchClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(brokerSearchClientMock);
  }

  @Test
  void whenGetBrokerByOrganizationIdThenInvokeClient() {
    // Given
    Long organizationId = 1L;
    String accessToken = "accessToken";
    Broker broker = new Broker();

    Mockito.when(brokerSearchClientMock.getBrokerByOrganizationId(organizationId, accessToken))
      .thenReturn(broker);

    // When
    Broker result = brokerService.getBrokerByOrganizationId(organizationId, accessToken);

    // Then
    Assertions.assertSame(broker, result);
  }
}
