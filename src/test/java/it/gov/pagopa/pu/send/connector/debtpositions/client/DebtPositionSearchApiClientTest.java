package it.gov.pagopa.pu.send.connector.debtpositions.client;

import it.gov.pagopa.pu.debtposition.client.generated.DebtPositionSearchControllerApi;
import it.gov.pagopa.pu.debtposition.dto.generated.CollectionModelDebtPosition;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.debtposition.dto.generated.PagedModelDebtPositionEmbedded;
import it.gov.pagopa.pu.send.connector.debtpositions.config.DebtPositionApisHolder;
import it.gov.pagopa.pu.send.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class DebtPositionSearchApiClientTest {

  @Mock
  private DebtPositionApisHolder apisHolder;
  @Mock
  private DebtPositionSearchControllerApi debtPositionSearchApiMock;

  private DebtPositionSearchApiClient debtPositionSearchApiClient;

  @BeforeEach
  void setUp() {
    debtPositionSearchApiClient = new DebtPositionSearchApiClient(apisHolder);
  }

  @Test
  void givenValidRequestWhenFindDebtPositionByInstallmentThenVerifyResponse() {
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "accessToken";

    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(apisHolder.getDebtPositionSearchApi(accessToken))
      .thenReturn(debtPositionSearchApiMock);
    Mockito.when(debtPositionSearchApiMock.crudDebtPositionsFindByOrganizationIdAndInstallmentNav(organizationId, nav, Constants.ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(new CollectionModelDebtPosition(new PagedModelDebtPositionEmbedded(List.of(expectedResult)), null));

    // When
    DebtPosition result = debtPositionSearchApiClient.findDebtPositionByInstallment(organizationId, nav, accessToken);

    // Then
    assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentDebtPositionIdWhenFindDebtPositionByInstallmentThenReturnNull() {
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "accessToken";

    Mockito.when(apisHolder.getDebtPositionSearchApi(accessToken))
      .thenReturn(debtPositionSearchApiMock);
    Mockito.when(debtPositionSearchApiMock.crudDebtPositionsFindByOrganizationIdAndInstallmentNav(organizationId, nav, Constants.ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(new CollectionModelDebtPosition(new PagedModelDebtPositionEmbedded(), null));

    // When
    DebtPosition result = debtPositionSearchApiClient.findDebtPositionByInstallment(organizationId, nav, accessToken);

    // Then
    assertNull(result);
  }
}
