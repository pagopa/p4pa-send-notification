package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.BrokerConfigurationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokerConfigurationSearchClientTest {

  @Mock
  private OrganizationApisHolder apisHolderMock;
  @Mock
  private BrokerConfigurationSearchControllerApi brokerConfigurationSearchControllerApiMock;

  private BrokerConfigurationSearchClient brokerConfigurationSearchClient;

  @BeforeEach
  void setUp() {
    brokerConfigurationSearchClient = new BrokerConfigurationSearchClient(apisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      apisHolderMock,
      brokerConfigurationSearchControllerApiMock
      );
  }

  @Test
  void whenGetBrokerConfigurationByOrganizationIdThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;
    BrokerConfiguration expectedResult = new BrokerConfiguration();

    when(apisHolderMock.getBrokerConfigurationSearchControllerApi(accessToken))
      .thenReturn(brokerConfigurationSearchControllerApiMock);
    when(brokerConfigurationSearchControllerApiMock.crudBrokerConfigurationsFindByOrganizationId(orgId))
      .thenReturn(expectedResult);

    // When
    BrokerConfiguration result = brokerConfigurationSearchClient.getBrokerConfigurationByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotFoundWhenGetBrokerConfigurationByOrganizationIdThenNull() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;

    when(apisHolderMock.getBrokerConfigurationSearchControllerApi(accessToken))
      .thenReturn(brokerConfigurationSearchControllerApiMock);
    when(brokerConfigurationSearchControllerApiMock.crudBrokerConfigurationsFindByOrganizationId(orgId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    BrokerConfiguration result = brokerConfigurationSearchClient.getBrokerConfigurationByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
