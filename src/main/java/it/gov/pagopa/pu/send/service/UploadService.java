package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import java.util.Optional;

public interface UploadService {
  Optional<String> uploadFile(String sendNotidicationId, DocumentDTO documentDTO);
}
