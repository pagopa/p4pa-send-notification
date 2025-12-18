package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.BrokerSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class BrokerSearchClientTest {

  @Mock
  private OrganizationApisHolder apisHolderMock;
  @Mock
  private BrokerSearchControllerApi brokerSearchControllerApiMock;

  private BrokerSearchClient brokerSearchClient;

  @BeforeEach
  void setUp() {
    brokerSearchClient = new BrokerSearchClient(apisHolderMock);
  }

  @Test
  void whenGetBrokerByOrganizationIdThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;
    Broker expectedResult = new Broker();

    Mockito.when(apisHolderMock.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    Mockito.when(brokerSearchControllerApiMock.crudBrokersFindByBrokeredOrganizationId(String.valueOf(orgId)))
      .thenReturn(expectedResult);

    // When
    Broker result = brokerSearchClient.getBrokerByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentOrganizationIdWhenGetBrokerByOrganizationIdThenNull() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;

    Mockito.when(apisHolderMock.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    Mockito.when(brokerSearchControllerApiMock.crudBrokersFindByBrokeredOrganizationId(String.valueOf(orgId)))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Broker result = brokerSearchClient.getBrokerByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
