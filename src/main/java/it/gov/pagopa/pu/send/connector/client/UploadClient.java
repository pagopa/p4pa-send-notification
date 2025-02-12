package it.gov.pagopa.pu.send.connector.client;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import java.util.Optional;

public interface UploadClient {
  Optional<String> uploadFileToS3(DocumentDTO documentDTO, byte[] fileBytes);
}
