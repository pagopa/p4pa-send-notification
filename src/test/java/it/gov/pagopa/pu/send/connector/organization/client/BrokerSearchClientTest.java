package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.BrokerSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.when;

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

    when(apisHolderMock.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    when(brokerSearchControllerApiMock.crudBrokersFindByBrokeredOrganizationId(String.valueOf(orgId)))
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

    when(apisHolderMock.getBrokerSearchControllerApi(accessToken))
      .thenReturn(brokerSearchControllerApiMock);
    when(brokerSearchControllerApiMock.crudBrokersFindByBrokeredOrganizationId(String.valueOf(orgId)))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Broker result = brokerSearchClient.getBrokerByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
