package it.gov.pagopa.pu.send.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

import it.gov.pagopa.pu.send.util.AESUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileStorerServiceTest {

  private static final String FILE_ENCRYPT_PASSWORD = "testPassword123";
  private static final String SEND_FILE_PATH = "send";
  private static final String SHARED_FOLDER = "/shared";
  private static final String FILECONTENT = "TEST FILE HASH P4PA SEND";

  private FileStorerService fileStorerService;

  @BeforeEach
  void setUp() {
    fileStorerService = new FileStorerService(
      FILE_ENCRYPT_PASSWORD,
      SEND_FILE_PATH,
      SHARED_FOLDER
    );
  }

  @Test
  void givenCorrectPathWhenRetrieveFileThenReturnDecryptFile() {
    // Given
    Long organizationId = 1L;
    String sendNotificationId = "SENDID";
    String fileName = "test.pdf";
    InputStream expectedInputStream = new ByteArrayInputStream(FILECONTENT.getBytes());
    Path expectedPath = Path.of(SHARED_FOLDER, String.valueOf(organizationId), SEND_FILE_PATH, sendNotificationId);

    try (MockedStatic<AESUtils> aesUtils = Mockito.mockStatic(AESUtils.class)) {
      aesUtils.when(() -> AESUtils.decrypt(
        eq(FILE_ENCRYPT_PASSWORD),
        eq(expectedPath),
        eq(fileName)
      )).thenReturn(expectedInputStream);

      // When
      InputStream result = fileStorerService.retrieveFile(organizationId, sendNotificationId, fileName);

      // Then
      assertNotNull(result);
      assertEquals(expectedInputStream, result);
    }
  }

  @Test
  void givenCorrectParametersWhenDecryptFileThenReturnInputStream() {
    // Given
    Path filePath = Path.of("/test/path");
    String fileName = "test.pdf";
    InputStream expectedInputStream = new ByteArrayInputStream(FILECONTENT.getBytes());

    try (MockedStatic<AESUtils> aesUtils = Mockito.mockStatic(AESUtils.class)) {
      aesUtils.when(() -> AESUtils.decrypt(
        eq(FILE_ENCRYPT_PASSWORD),
        eq(filePath),
        eq(fileName)
      )).thenReturn(expectedInputStream);

      // When
      InputStream result = fileStorerService.decryptFile(filePath, fileName);

      // Then
      assertNotNull(result);
      assertEquals(expectedInputStream, result);
    }
  }

}
