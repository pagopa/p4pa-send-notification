package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.exception.UploadFileException;
import it.gov.pagopa.pu.send.util.AESUtils;
import java.io.InputStream;
import java.nio.file.Path;

import it.gov.pagopa.pu.send.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

  public String saveToSharedFolder(Long organizationId, String sendNotificationId, MultipartFile file, String fileName) {
    if (file == null) {
      throw new UploadFileException("File is mandatory");
    }

    fileName = org.springframework.util.StringUtils.cleanPath(StringUtils.defaultString(fileName));
    FileUtils.validateFilename(fileName);
    Path relativePath =  buildRelativeSendPath(organizationId, sendNotificationId);
    try {
      AESUtils.encryptAndSave(fileEncryptPassword, file.getInputStream(), relativePath, fileName);
    } catch (Exception e) {
      throw new UploadFileException("Error uploading file to shared folder %s".formatted(relativePath));
    }

    return relativePath+"/"+fileName;
  }
}
