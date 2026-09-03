package it.gov.pagopa.pu.send.connector.organization.client;

import it.gov.pagopa.pu.organization.client.generated.OrgSubUnitEntityControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.OrgSubUnit;
import it.gov.pagopa.pu.send.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgSubUnitApiClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolder;
  @Mock
  private OrgSubUnitEntityControllerApi orgSubUnitEntityControllerApiMock;

  private OrgSubUnitApiClient orgSubUnitApiClient;

  @BeforeEach
  void setUp() {
    orgSubUnitApiClient = new OrgSubUnitApiClient(organizationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolder,
      orgSubUnitEntityControllerApiMock
    );
  }

  @Test
  void whenGetOrgSubUnitByIdThenInvokeWithAccessToken() {
    Long organizationId = 1L;
    String orgSubUnitCode = "01";
    String accessToken = "accessToken";
    OrgSubUnit expectedResult = new OrgSubUnit();

    when(organizationApisHolder.getOrgSubUnitEntityControllerApi(accessToken))
      .thenReturn(orgSubUnitEntityControllerApiMock);
    when(orgSubUnitEntityControllerApiMock.crudGetOrgsubunit(organizationId + "-" + orgSubUnitCode))
      .thenReturn(expectedResult);

    OrgSubUnit result = orgSubUnitApiClient.getOrgSubUnitById(organizationId, orgSubUnitCode, accessToken);

    assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentOrgSubUnitIdWhenGetOrgSubUnitByIdThenReturnNull() {
    Long organizationId = 1L;
    String orgSubUnitCode = "01";
    String accessToken = "accessToken";

    when(organizationApisHolder.getOrgSubUnitEntityControllerApi(accessToken))
      .thenReturn(orgSubUnitEntityControllerApiMock);
    when(orgSubUnitEntityControllerApiMock.crudGetOrgsubunit(organizationId + "-" + orgSubUnitCode))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    OrgSubUnit result = orgSubUnitApiClient.getOrgSubUnitById(organizationId, orgSubUnitCode, accessToken);

    assertNull(result);
  }
}
