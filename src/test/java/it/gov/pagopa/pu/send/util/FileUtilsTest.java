package it.gov.pagopa.pu.send.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

  @Test
  void givenValidFileWhenCalculateFileHashThenVerifyHash()
    throws NoSuchAlgorithmException, IOException {
    // Given
    InputStream inputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    String expectedHash = "9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=";
    // When
    String actualHash = FileUtils.calculateFileHash(inputStream);

    // Then
    assertEquals(expectedHash, actualHash);
  }
}
