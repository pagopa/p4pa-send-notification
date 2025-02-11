package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public interface UploadService {
  Optional<String> uploadFileToS3(String sendNotidicationId, DocumentDTO documentDTO) throws IOException, NoSuchAlgorithmException;
}
