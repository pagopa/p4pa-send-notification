package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.send.connector.debtpositions.client.DebtPositionApiClient;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionApiClient debtPositionApiClient;

  public DebtPositionServiceImpl(DebtPositionApiClient debtPositionApiClient) {
    this.debtPositionApiClient = debtPositionApiClient;
  }

  @Override
  public DebtPositionDTO findDebtPositionByInstallment(Long organizationId, String nav, String accessToken) {
    return debtPositionApiClient.findDebtPositionByInstallment(organizationId, nav, accessToken);
  }
}
