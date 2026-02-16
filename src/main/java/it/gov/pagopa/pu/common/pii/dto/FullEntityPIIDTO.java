package it.gov.pagopa.pu.common.pii.dto;

import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;

public interface FullEntityPIIDTO<E extends NoPIIEntity<P>, P extends PIIDTO> {
  E getNoPII();
  void setNoPII(E noPII);
}
