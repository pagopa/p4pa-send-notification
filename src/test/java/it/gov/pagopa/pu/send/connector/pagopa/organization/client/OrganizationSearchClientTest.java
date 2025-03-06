package it.gov.pagopa.pu.send.connector.pagopa.organization.client;

import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.pagopa.organization.config.OrganizationApisHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class OrganizationSearchClientTest {

  @Mock
  private OrganizationApisHolder apisHolder;
  @Mock
  private OrganizationSearchControllerApi organizationSearchControllerApiMock;

  private OrganizationSearchClient organizationSearchClient;

  @BeforeEach
  void setUp() {
    organizationSearchClient = new OrganizationSearchClient(apisHolder);
  }

  @Test
  void givenValidRequestWhenFindByIpaCodeThenVerifyResponse() {
    // Given
    String accessToken = "accessToken";
    String ipaCode = "ipaCode";
    Organization org = new Organization();

    Mockito.when(apisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByIpaCode(ipaCode))
      .thenReturn(org);

    // When
    Organization result = organizationSearchClient.findByIpaCode(ipaCode, accessToken);

    // Then
    assertSame(org, result);
  }
}
