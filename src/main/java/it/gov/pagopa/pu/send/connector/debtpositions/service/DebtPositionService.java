package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

public interface DebtPositionService {
  DebtPositionDTO findDebtPositionByInstallment(Long organizationId, String nav, String accessToken);
}
