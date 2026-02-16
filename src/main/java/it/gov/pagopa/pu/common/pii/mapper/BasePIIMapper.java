package it.gov.pagopa.pu.common.pii.mapper;

import it.gov.pagopa.pu.common.pii.dto.FullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;
import org.springframework.data.util.Pair;

public abstract class BasePIIMapper<F extends FullEntityPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO> {

  public final Pair<E, P> map(F fullDTO){
    E noPii = extractNoPiiEntity(fullDTO);
    if (fullDTO.getNoPII() != null) {
      noPii.setPersonalDataId(fullDTO.getNoPII().getPersonalDataId());
    }

    P pii = extractPiiDto(fullDTO);
    return Pair.of(noPii, pii);
  }

  protected abstract E extractNoPiiEntity(F fullDTO);
  protected abstract P extractPiiDto(F fullDTO);

  public abstract F map(E noPii);
}
