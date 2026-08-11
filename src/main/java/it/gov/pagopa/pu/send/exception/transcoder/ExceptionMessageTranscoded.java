package it.gov.pagopa.pu.send.exception.transcoder;

import it.gov.pagopa.pu.send.dto.generated.ErrorFieldDTO;
import lombok.Data;

import java.util.List;

@Data
public class ExceptionMessageTranscoded {
  private final String code;
  private final String message;
  private final List<ErrorFieldDTO> fields;
}
