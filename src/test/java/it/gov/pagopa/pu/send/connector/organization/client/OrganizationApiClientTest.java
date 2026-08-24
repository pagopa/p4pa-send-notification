package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationApiClientTest {

  @Mock
  private OrganizationApisHolder apisHolder;
  @Mock
  private OrganizationApi organizationApiMock;
  @Mock
  private OrganizationEntityControllerApi organizationEntityControllerApiMock;
  @Mock
  private OrganizationSearchControllerApi organizationSearchControllerApiMock;

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

    when(apisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, null))
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

    when(apisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationApiKey(organizationId, OrganizationApiKeyType.SEND, null))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

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

    when(apisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    when(organizationEntityControllerApiMock.crudGetOrganization(orgId))
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

    when(apisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    when(organizationEntityControllerApiMock.crudGetOrganization(orgId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Organization result = organizationApiClient.findByOrganizationId(Long.valueOf(orgId), accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void whenFindByOrgFiscalCodeAndSegregationCodeThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String orgFiscalCode = "00002";
    String segregationCode = "01";
    Organization expectedResult = new Organization();

    when(apisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode))
      .thenReturn(expectedResult);

    // When
    Optional<Organization> result = organizationApiClient.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(expectedResult, result.get());
  }

  @Test
  void givenNotExistentOrganizationIdWhenFindByOrgFiscalCodeAndSegregationCodeThenEmpty() {
    String accessToken = "ACCESSTOKEN";
    String orgFiscalCode = "00002";
    String segregationCode = "01";

    when(apisHolder.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Optional<Organization> result = organizationApiClient.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken);

    // Then
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void whenFindOrganizationStationThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String stationId = "STATIONID";
    OrganizationStationDTO expectedResult = new OrganizationStationDTO();

    when(apisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationStation(organizationId, stationId))
      .thenReturn(expectedResult);

    // When
    OrganizationStationDTO result = organizationApiClient.findOrganizationStation(organizationId, stationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentStationIdWhenFindOrganizationStationThenNull() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String stationId = "STATIONID";

    when(apisHolder.getOrganizationApi(accessToken))
      .thenReturn(organizationApiMock);
    when(organizationApiMock.getOrganizationStation(organizationId, stationId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    OrganizationStationDTO result = organizationApiClient.findOrganizationStation(organizationId, stationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }
}
