package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SendUploadServiceImpl implements SendUploadService {

  private final SendUploadClient client;

  public SendUploadServiceImpl(SendUploadClient client) {
    this.client = client;
  }

  @Override
  public Optional<String> upload(DocumentDTO documentDTO, byte[] fileBytes) {
    return client.upload(documentDTO, fileBytes);
  }
}
