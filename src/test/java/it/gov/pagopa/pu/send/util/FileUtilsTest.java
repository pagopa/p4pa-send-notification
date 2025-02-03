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

    String expectedHash = "ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ==";
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
