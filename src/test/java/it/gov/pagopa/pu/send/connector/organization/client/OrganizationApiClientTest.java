package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class OrganizationApiClientTest {

  @Mock
  private OrganizationApisHolder apisHolder;
  @Mock
  private OrganizationApi organizationApiMock;
  @Mock
  private OrganizationEntityControllerApi organizationEntityControllerApiMock;

  private OrganizationApiClient organizationApiClient;

  @BeforeEach
  void setUp() {
    organizationApiClient = new OrganizationApiClient(apisHolder);
  }

  @Test
  void givenValidRequestWhenGetOrganizationApiKeyThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String accessToken = "accessToken";
    String apiKey = "apiKey";

    Mockito.when(apisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    Mockito.when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND))
      .thenReturn(apiKey);

    // When
    String result = organizationApiClient.getOrganizationApiKey(organizationId, accessToken);

    // Then
    assertSame(apiKey, result);
  }

  @Test
  void givenNotExistentOrganizationIdWhenGetOrganizationApiKeyThenReturnNull() {
    // Given
    Long organizationId = 1L;
    String accessToken = "accessToken";

    Mockito.when(apisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    Mockito.when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    String result = organizationApiClient.getOrganizationApiKey(organizationId, accessToken);

    // Then
    assertNull(result);
  }

  //region findByOrganizationId test
  @Test
  void whenFindByIdThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String orgId = "1";
    Organization expectedResult = new Organization();

    Mockito.when(apisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(orgId))
      .thenReturn(expectedResult);

    // When
    Organization result = organizationApiClient.findByOrganizationId(Long.valueOf(orgId), accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentOrganizationIdWhenFindByIdThenNull() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String orgId = "1";

    Mockito.when(apisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(orgId))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Organization result = organizationApiClient.findByOrganizationId(Long.valueOf(orgId), accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
