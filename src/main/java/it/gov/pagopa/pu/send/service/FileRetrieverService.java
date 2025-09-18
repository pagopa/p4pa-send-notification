package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.util.AESUtils;
import java.io.InputStream;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileRetrieverService {
  private final String fileEncryptPassword;
  private final String sendFilePath;
  private final String sharedFolder;

  public FileRetrieverService(@Value("${app.fileEncryptPassword}") String fileEncryptPassword,
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
}
