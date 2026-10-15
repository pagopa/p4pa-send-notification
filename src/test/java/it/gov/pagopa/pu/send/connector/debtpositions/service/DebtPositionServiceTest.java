package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.send.connector.debtpositions.client.DebtPositionApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceTest {

  @Mock
  private DebtPositionApiClient debtPositionApiClientMock;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(debtPositionApiClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionApiClientMock);
  }

  @Test
  void whenGetOrganizationApiKeyThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String nav = "NAV";
    String accessToken = "accessToken";

    DebtPositionDTO expectedResult = new DebtPositionDTO();

    when(debtPositionApiClientMock.findDebtPositionByInstallment(organizationId, nav, accessToken))
      .thenReturn(expectedResult);

    // When
    DebtPositionDTO result = service.findDebtPositionByInstallment(organizationId, nav, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
