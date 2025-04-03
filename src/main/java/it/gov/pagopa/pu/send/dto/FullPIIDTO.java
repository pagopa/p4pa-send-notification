package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.model.NoPIIEntity;

public interface FullPIIDTO<E extends NoPIIEntity<P>, P extends PIIDTO> {
  E getNoPII();
  void setNoPII(E noPII);
}
