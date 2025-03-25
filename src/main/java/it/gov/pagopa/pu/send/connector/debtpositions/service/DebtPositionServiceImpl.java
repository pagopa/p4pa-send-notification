package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.send.connector.debtpositions.client.DebtPositionSearchApiClient;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionSearchApiClient debtPositionSearchApiClient;

  public DebtPositionServiceImpl(DebtPositionSearchApiClient debtPositionSearchApiClient) {
    this.debtPositionSearchApiClient = debtPositionSearchApiClient;
  }

  @Override
  public DebtPosition findDebtPositionByInstallment(Long organizationId, String nav, String accessToken) {
    return debtPositionSearchApiClient.findDebtPositionByInstallment(organizationId, nav, accessToken);
  }
}
