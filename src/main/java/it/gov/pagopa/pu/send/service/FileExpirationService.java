package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.FileExpirationResponseDTO;

public interface FileExpirationService {
  FileExpirationResponseDTO deleteExpiredLegalFacts(String sendNotificationId, String accessToken);
}
