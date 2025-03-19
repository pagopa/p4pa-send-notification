package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import it.gov.pagopa.pu.send.util.FileUtils;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
    try {
      InputStream inputStream = fileRetrieverService.retrieveFile(organizationId, fileName);
      if(inputStream==null)
        throw new FileNotFoundException("File not found: " + documentDTO.getFileName());

      if(!FileUtils.calculateFileHash(inputStream).equals(documentDTO.getDigest()))
        throw new InvalidSignatureException("File "+documentDTO.getFileName()+" has not a valid signature");

      byte[] fileBytes = inputStream.readAllBytes();
      return sendUploadClient.upload(documentDTO, fileBytes);
    } catch (Exception e) {
      throw new UploadFileException(e.getMessage());
    }
  }
}
