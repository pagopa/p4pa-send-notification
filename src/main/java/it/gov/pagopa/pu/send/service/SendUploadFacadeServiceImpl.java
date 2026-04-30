package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class SendUploadFacadeServiceImpl implements SendUploadFacadeService {

  private final SendUploadClient sendUploadClient;
  private final FileStorerService fileStorerService;

  public SendUploadFacadeServiceImpl(SendUploadClient sendUploadClient,
    FileStorerService fileStorerService) {
    this.sendUploadClient = sendUploadClient;
    this.fileStorerService = fileStorerService;
  }

  @Override
  public Optional<String> uploadFile(Long organizationId, String sendNotificationId, DocumentDTO documentDTO) {
    String fileName = sendNotificationId + "_" + documentDTO.getFileName();
    try(InputStream inputStream = fileStorerService.retrieveFile(organizationId, sendNotificationId, fileName)) {
      if(inputStream==null)
        throw new FileNotFoundException("File not found: " + documentDTO.getFileName());
      byte[] fileBytes = inputStream.readAllBytes();
      return sendUploadClient.upload(documentDTO, fileBytes);
    } catch (Exception e) {
      throw new UploadFileException(ErrorCodeConstants.ERROR_CODE_UPLOAD_ERROR, e.getMessage());
    }
  }
}
