package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.client.UploadClientImpl;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import it.gov.pagopa.pu.send.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UploadServiceImpl implements UploadService{

  private final UploadClientImpl uploadClient;

  public UploadServiceImpl(UploadClientImpl uploadClient) {
    this.uploadClient = uploadClient;
  }

  @Override
  public Optional<String> uploadFile(String sendNotificationId, DocumentDTO documentDTO) {
    //TODO edit file retrieve with P4ADEV-2148
    String filePath = "src/main/resources/tmp/" + "sendNotificationId" + "_" + documentDTO.getFileName();
    File file = new File(filePath);
    try {

      if (!file.exists() || !file.isFile())
        throw new FileNotFoundException("File not found: " + documentDTO.getFileName());

      if(!FileUtils.calculateFileHash(file).equals(documentDTO.getDigest()))
        throw new InvalidSignatureException("File "+documentDTO.getFileName()+" has not a valid signature");

      byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
      return uploadClient.upload(documentDTO, fileBytes);
    } catch (Exception e) {
      throw new UploadFileException(e.getMessage());
    }
  }
}
