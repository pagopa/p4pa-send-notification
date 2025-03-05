package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

import it.gov.pagopa.pu.send.connector.pagopa.organization.client.OrganizationApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationApiClient clientMock;

  private OrganizationService service;

  @BeforeEach
  void init(){
    service = new OrganizationServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetOrganizationApiKeyThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String keyType = "SEND";
    String accessToken = "accessToken";
    String apiKey = "apiKey";

    Mockito.when(clientMock.getOrganizationApiKey(organizationId, keyType, accessToken))
      .thenReturn(apiKey);

    // When
    String result = service.getOrganizationApiKey(String.valueOf(organizationId), keyType, accessToken);

    // Then
    Assertions.assertSame(apiKey, result);
  }
}
