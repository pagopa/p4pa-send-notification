package it.gov.pagopa.pu.send.connector.pagopa.organization.client;

import it.gov.pagopa.pu.organization.client.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.send.connector.pagopa.organization.config.OrganizationApisHolder;
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
}
