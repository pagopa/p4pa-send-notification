package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;

public interface DebtPositionService {
  DebtPosition findDebtPositionByInstallment(Long organizationId, String nav, String accessToken);
}
