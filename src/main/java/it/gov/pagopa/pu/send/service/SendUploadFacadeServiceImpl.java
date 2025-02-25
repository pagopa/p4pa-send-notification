package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import it.gov.pagopa.pu.send.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendUploadFacadeServiceImpl implements SendUploadFacadeService {

  private final SendUploadClient sendUploadClient;

  public SendUploadFacadeServiceImpl(SendUploadClient sendUploadClient) {
    this.sendUploadClient = sendUploadClient;
  }

  @Override
  public Optional<String> uploadFile(String sendNotificationId, DocumentDTO documentDTO) {
    //TODO edit file retrieve with P4ADEV-2148
    String fileName = "sendNotificationId" + "_" + documentDTO.getFileName();
    Path resourceDirectory = Paths.get("src","main","resources","tmp");
    log.info("File Path: {}", resourceDirectory.resolve(fileName));
    try {
      File file = new File(resourceDirectory.resolve(fileName).toString());
      if (!file.exists() || !file.isFile())
        throw new FileNotFoundException("File not found: " + documentDTO.getFileName());

      if(!FileUtils.calculateFileHash(file).equals(documentDTO.getDigest()))
        throw new InvalidSignatureException("File "+documentDTO.getFileName()+" has not a valid signature");

      byte[] fileBytes = Files.readAllBytes(file.toPath());
      return sendUploadClient.upload(documentDTO, fileBytes);
    } catch (Exception e) {
      throw new UploadFileException(e.getMessage());
    }
  }
}
