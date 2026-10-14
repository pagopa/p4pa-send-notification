package it.gov.pagopa.pu.send.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.send.connector.debtpositions.config.DebtPositionApisHolder;
import it.gov.pagopa.pu.send.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DebtPositionApiClient {

  private final DebtPositionApisHolder debtPositionApisHolder;

  public DebtPositionApiClient(DebtPositionApisHolder debtPositionApisHolder) {
    this.debtPositionApisHolder = debtPositionApisHolder;
  }

  public DebtPositionDTO findDebtPositionByInstallment(Long organizationId, String nav, String accessToken) {
      List<DebtPositionDTO> debtPositions = Objects.requireNonNull(debtPositionApisHolder.getDebtPositionApi(accessToken)
        .getDebtPositionsByOrganizationIdAndNav(organizationId, nav, Constants.ORDINARY_DEBT_POSITION_ORIGINS));
     if(CollectionUtils.isEmpty(debtPositions)){
       return null;
     } else {
       return debtPositions.getFirst();
     }
  }

  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(
    Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken) {
    return debtPositionApisHolder.getInstallmentApi(accessToken)
      .getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList);
  }
}
