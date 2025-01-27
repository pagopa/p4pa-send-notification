package it.gov.pagopa.pu.send.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

  @Test
  void givenValidFileWhenCalculateFileHashThenVerifyHash()
    throws NoSuchAlgorithmException, IOException {
    // Given
    File tempFile = File.createTempFile("testFile", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }

    String expectedHash = "f5ef4bb18a78a90e1b8f2188e0ca7f8e604dda329e84a4d36a89ccaf500910f5";
    // When
    String actualHash = FileUtils.calculateFileHash(tempFile);

    // Then
    assertEquals(expectedHash, actualHash);
  }

  @Test
  void givenNotExistsFileWhenCalculateFileHashThenException() {
    // Given
    File nonExistentFile = new File("FILENOTFOUND.txt");

    // Then
    assertThrows(IOException.class, () -> FileUtils.calculateFileHash(nonExistentFile));
  }
}
