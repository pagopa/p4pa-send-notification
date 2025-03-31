package it.gov.pagopa.pu.send.connector.debtpositions.client;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.send.connector.debtpositions.config.DebtPositionApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class DebtPositionSearchApiClient {

  private final DebtPositionApisHolder debtPositionApisHolder;

  public DebtPositionSearchApiClient(DebtPositionApisHolder debtPositionApisHolder) {
    this.debtPositionApisHolder = debtPositionApisHolder;
  }

  public DebtPosition findDebtPositionByInstallment(Long organizationId, String nav, String accessToken) {
    try{
      return debtPositionApisHolder.getDebtPositionSearchApi(accessToken)
        .crudDebtPositionsFindByOrganizationIdAndInstallmentNav(organizationId, nav);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find DebtPosition related to organizationId {} and NAV {}", organizationId, nav);
      return null;
    }
  }

}
