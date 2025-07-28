package it.gov.pagopa.pu.send.connector.debtpositions.client;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.send.connector.debtpositions.config.DebtPositionApisHolder;
import it.gov.pagopa.pu.send.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DebtPositionSearchApiClient {

  private final DebtPositionApisHolder debtPositionApisHolder;

  public DebtPositionSearchApiClient(DebtPositionApisHolder debtPositionApisHolder) {
    this.debtPositionApisHolder = debtPositionApisHolder;
  }

  public DebtPosition findDebtPositionByInstallment(Long organizationId, String nav, String accessToken) {
      List<DebtPosition> debtPositions = Objects.requireNonNull(debtPositionApisHolder.getDebtPositionSearchApi(accessToken)
          .crudDebtPositionsFindByOrganizationIdAndInstallmentNav(organizationId, nav, Constants.ORDINARY_DEBT_POSITION_ORIGINS)
          .getEmbedded())
        .getDebtPositions();
     if(CollectionUtils.isEmpty(debtPositions)){
       return null;
     } else {
       return debtPositions.getFirst();
     }
  }

}
