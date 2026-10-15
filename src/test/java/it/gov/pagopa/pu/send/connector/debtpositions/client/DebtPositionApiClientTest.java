package it.gov.pagopa.pu.send.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.send.connector.debtpositions.config.DebtPositionApisHolder;
import it.gov.pagopa.pu.send.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionApiClientTest {

  @Mock
  private DebtPositionApisHolder apisHolder;
  @Mock
  private DebtPositionApi debtPositionApiMock;

  private DebtPositionApiClient debtPositionApiClient;

  @BeforeEach
  void setUp() {
    debtPositionApiClient = new DebtPositionApiClient(apisHolder);
  }

  @Test
  void givenValidRequestWhenFindDebtPositionByInstallmentThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "accessToken";

    DebtPositionDTO expectedResult = new DebtPositionDTO();

    when(apisHolder.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    when(debtPositionApiMock.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, Constants.ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(List.of(expectedResult));

    // When
    DebtPositionDTO result = debtPositionApiClient.findDebtPositionByInstallment(organizationId, nav, accessToken);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentDebtPositionIdWhenFindDebtPositionByInstallmentThenReturnNull() {
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "accessToken";

    when(apisHolder.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    when(debtPositionApiMock.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, Constants.ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(List.of());

    // When
    DebtPositionDTO result = debtPositionApiClient.findDebtPositionByInstallment(organizationId, nav, accessToken);

    // Then
    assertNull(result);
  }
}
