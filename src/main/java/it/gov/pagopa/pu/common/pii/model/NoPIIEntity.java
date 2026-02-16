package it.gov.pagopa.pu.common.pii.model;

import it.gov.pagopa.pu.common.pii.dto.PIIDTO;

public interface NoPIIEntity<P extends PIIDTO> {
  void setPersonalDataId(Long personalDataId);
  Long getPersonalDataId();
}
