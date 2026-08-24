package it.gov.pagopa.pu.send.config.rest;

import it.gov.pagopa.pu.send.dto.generated.ErrorFieldDTO;

import java.util.List;

public record PuErrorDTO(
  String category,
  String code,
  String message,
  List<ErrorFieldDTO> fields
) {
}
