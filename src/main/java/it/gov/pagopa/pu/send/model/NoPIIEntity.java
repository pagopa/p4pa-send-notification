package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.PIIDTO;

public interface NoPIIEntity<P extends PIIDTO> {
  void setPersonalDataId(Long personalDataId);
  Long getPersonalDataId();
}
