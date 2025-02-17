package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.dto.DocumentDTO;

import java.util.Optional;

public interface SendUploadService {
  Optional<String> upload(DocumentDTO documentDTO, byte[] fileBytes);
}
