package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.exception.DeleteFileException;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import it.gov.pagopa.pu.send.util.AESUtils;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import it.gov.pagopa.pu.send.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static it.gov.pagopa.pu.send.service.SendNotificationServiceImpl.concatenatePaths;

@Slf4j
@Service
public class FileStorerService {
  private final String fileEncryptPassword;
  private final String sendFilePath;
  private final String sharedFolder;

  public FileStorerService(@Value("${app.fileEncryptPassword}") String fileEncryptPassword,
                           @Value("${folders.send-file-folder}") String sendFilePath,
                           @Value("${folders.shared}") String sharedFolder) {
    this.fileEncryptPassword = fileEncryptPassword;
    this.sendFilePath = sendFilePath;
    this.sharedFolder = sharedFolder;
  }

  public InputStream retrieveFile(Long organizationId, String sendNotificationId, String fileName) {
    return decryptFile(buildRelativeSendPath(organizationId, sendNotificationId), fileName);
  }

  public InputStream decryptFile(Path filePath, String fileName) {
    return AESUtils.decrypt(fileEncryptPassword, filePath, fileName);
  }

  public Path buildRelativeSendPath(Long organizationId, String sendNotificationId) {
    return Path.of(sharedFolder, String.valueOf(organizationId), sendFilePath, sendNotificationId);
  }

  public String saveToSharedFolder(Long organizationId, String sendNotificationId, InputStream inputStream, String fileName) {
    if (inputStream == null) {
      throw new UploadFileException(ErrorCodeConstants.ERROR_CODE_MANDATORY_FILE, "InputStream is mandatory");
    }

    FileUtils.validateFilename(fileName);
    fileName = org.springframework.util.StringUtils.cleanPath(StringUtils.defaultString(fileName));

    Path relativeSendPath =  buildRelativeSendPath(organizationId, sendNotificationId);
    Path absolutePath = concatenatePaths(relativeSendPath.toString(), fileName);
    try {
      AESUtils.encryptAndSave(fileEncryptPassword, inputStream, absolutePath.getParent(), absolutePath.getFileName().toString());
    } catch (Exception e) {
      throw new UploadFileException(ErrorCodeConstants.ERROR_CODE_UPLOAD_ERROR, "Error uploading file to shared folder %s".formatted(absolutePath));
    }

    return fileName;
  }

  public void deleteFromSharedFolder(Long organizationId, String sendNotificationId, String fileName) {
    if (StringUtils.isBlank(fileName)) {
      throw new DeleteFileException("filename is mandatory");
    }

    Path sendPath = buildRelativeSendPath(organizationId, sendNotificationId);
    Path filePath = sendPath.resolve(fileName + AESUtils.CIPHER_EXTENSION);
    try {
      if (!Files.deleteIfExists(filePath)) {
        log.info("Send notification file {} does not exist", fileName);
      }
    } catch (IOException e) {
      throw new IllegalStateException(
        "Send notification file %s could not be deleted".formatted(fileName), e);
    }
  }
}
