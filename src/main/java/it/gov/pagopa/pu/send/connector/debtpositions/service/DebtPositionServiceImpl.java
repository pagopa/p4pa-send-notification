package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.send.connector.debtpositions.client.DebtPositionApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

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

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken) {
    return debtPositionApiClient.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);
  }
}
