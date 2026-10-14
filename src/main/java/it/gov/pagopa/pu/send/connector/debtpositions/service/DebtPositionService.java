package it.gov.pagopa.pu.send.connector.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

import java.util.List;

public interface DebtPositionService {
  DebtPositionDTO findDebtPositionByInstallment(Long organizationId, String nav, String accessToken);
  List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOriginList, String accessToken);
}
