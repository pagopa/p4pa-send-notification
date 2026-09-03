package it.gov.pagopa.pu.send.connector.pdndservices.mapper;

import it.gov.pagopa.pu.pdndservices.dto.generated.PdndServicesErrorDTO;
import it.gov.pagopa.pu.send.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.send.dto.generated.ErrorFieldDTO;

public class PdndServicesErrorDTOMapper {

  private PdndServicesErrorDTOMapper() {
    /* This utility class should not be instantiated */
  }


  public static PuErrorDTO map(PdndServicesErrorDTO errorDTO) {
    return new PuErrorDTO(
      errorDTO.getCategory().getValue(),
      errorDTO.getCode(),
      errorDTO.getMessage(),
      errorDTO.getFields() != null
        ? errorDTO.getFields().stream()
        .map(field -> new ErrorFieldDTO(
          field.getField(),
          field.getError(),
          field.getMessage()
        ))
        .toList()
        : null
    );
  }
}
