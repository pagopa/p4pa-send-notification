package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SendUploadFacadeServiceImpl implements SendUploadFacadeService {

  private final SendUploadClient sendUploadClient;
  private final FileRetrieverService fileRetrieverService;

  public SendUploadFacadeServiceImpl(SendUploadClient sendUploadClient,
    FileRetrieverService fileRetrieverService) {
    this.sendUploadClient = sendUploadClient;
    this.fileRetrieverService = fileRetrieverService;
  }

  @Override
  public Optional<String> uploadFile(Long organizationId, String sendNotificationId, DocumentDTO documentDTO) {
    String fileName = sendNotificationId + "_" + documentDTO.getFileName();
    try(InputStream inputStream = fileRetrieverService.retrieveFile(organizationId, sendNotificationId, fileName)) {
      if(inputStream==null)
        throw new FileNotFoundException("File not found: " + documentDTO.getFileName());
      byte[] fileBytes = inputStream.readAllBytes();
      return sendUploadClient.upload(documentDTO, fileBytes);
    } catch (Exception e) {
      throw new UploadFileException(e.getMessage());
    }
  }
}
