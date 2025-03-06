package it.gov.pagopa.pu.send.connector.pagopa.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.pagopa.organization.client.OrganizationApiClient;
import it.gov.pagopa.pu.send.connector.pagopa.organization.client.OrganizationSearchClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationApiClient organizationApiClientMock;
  @Mock
  private OrganizationSearchClient organizationSearchClientMock;

  private OrganizationService service;

  @BeforeEach
  void init(){
    service = new OrganizationServiceImpl(organizationApiClientMock, organizationSearchClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(organizationApiClientMock);
  }

  @Test
  void whenGetOrganizationApiKeyThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String keyType = "SEND";
    String accessToken = "accessToken";
    String apiKey = "apiKey";

    Mockito.when(organizationApiClientMock.getOrganizationApiKey(organizationId, keyType, accessToken))
      .thenReturn(apiKey);

    // When
    String result = service.getOrganizationApiKey(organizationId, keyType, accessToken);

    // Then
    Assertions.assertSame(apiKey, result);
  }

  @Test
  void whenGetOrganizationByIpaCodeThenInvokeClient(){
    // Given
    String ipaCode = "ipaCode";
    String accessToken = "accessToken";
    Organization org = new Organization();

    Mockito.when(organizationSearchClientMock.findByIpaCode(ipaCode, accessToken))
      .thenReturn(org);

    // When
    Optional<Organization> result = service.getOrganizationByIpaCode(ipaCode, accessToken);

    // Then
    assertTrue(result.isPresent());
    Assertions.assertEquals(org, result.get());
  }
}
