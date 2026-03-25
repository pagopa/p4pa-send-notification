package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.organization.client.OrganizationApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationApiClient organizationApiClientMock;

  private OrganizationService service;

  @BeforeEach
  void init(){
    service = new OrganizationServiceImpl(organizationApiClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(organizationApiClientMock);
  }

  @Test
  void whenGetOrganizationApiKeyThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "accessToken";
    String apiKey = "apiKey";

    Mockito.when(organizationApiClientMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(apiKey);

    // When
    String result = service.getOrganizationApiKey(organizationId, accessToken);

    // Then
    Assertions.assertSame(apiKey, result);
  }

  @Test
  void whenGetOrganizationThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "accessToken";
    Organization organization = new Organization();

    Mockito.when(organizationApiClientMock.findByOrganizationId(organizationId, accessToken))
      .thenReturn(organization);

    // When
    Organization result = service.getOrganization(organizationId, accessToken);

    // Then
    Assertions.assertSame(organization, result);
  }

  @Test
  void whenFindByOrgFiscalCodeAndSegregationCodeThenInvokeClient(){
    // Given
    String orgFiscalCode = "orgFiscalCode";
    String segregationCode = "segregationCode";
    String accessToken = "accessToken";
    Organization organization = new Organization();

    Mockito.when(organizationApiClientMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.of(organization));

    // When
    Optional<Organization> result = service.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(organization, result.get());
  }

}
