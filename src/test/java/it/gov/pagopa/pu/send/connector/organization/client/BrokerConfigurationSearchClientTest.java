package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.BrokerConfigurationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import org.junit.jupiter.api.AfterEach;
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

    Mockito.when(apisHolderMock.getBrokerConfigurationSearchControllerApi(accessToken))
      .thenReturn(brokerConfigurationSearchControllerApiMock);
    Mockito.when(brokerConfigurationSearchControllerApiMock.crudBrokerConfigurationsFindByOrganizationId(orgId))
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

    Mockito.when(apisHolderMock.getBrokerConfigurationSearchControllerApi(accessToken))
      .thenReturn(brokerConfigurationSearchControllerApiMock);
    Mockito.when(brokerConfigurationSearchControllerApiMock.crudBrokerConfigurationsFindByOrganizationId(orgId))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    BrokerConfiguration result = brokerConfigurationSearchClient.getBrokerConfigurationByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
