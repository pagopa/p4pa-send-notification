package it.gov.pagopa.pu.send.citizen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataCipherServiceTest {

  private final DataCipherService service = new DataCipherService("PSW","PEPPER", new ObjectMapper());

  @Test
  void testEncrypt() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] cipher = service.encrypt(plain);
    String result = service.decrypt(cipher);

    // Then
    Assertions.assertEquals(plain, result);
  }

  @Test
  void testEncryptObj() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] cipher = service.encryptObj(plain);
    String result = service.decryptObj(cipher, String.class);

    // Then
    Assertions.assertEquals(plain, result);
  }

  @Test
  void testHash() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] hash = service.hash(plain);

    // Then
    Assertions.assertEquals("s+QUCtO7vYNzHCDrH03EVRGPZTyfIXwBKTRrgYWqwc4=", Base64.getEncoder().encodeToString(hash));
  }

  @Test
  void testHashNull() {
    // When
    byte[] hash = service.hash(null);

    // Then
    Assertions.assertNull(hash);
  }

  @Test
  void testEncryptObjThrowsIllegalStateException() {
    // When
    Object mockItem = Mockito.mock(Object.class);

    // Then
    Assertions.assertThrows(IllegalStateException.class, () -> service.encryptObj(mockItem));
  }

  @Test
  void testDecryptObjThrowsIllegalStateException() {
    // When
    Object mockItem = Mockito.mock(Object.class);
    byte[] hash = service.hash(mockItem.toString());

    // Then
    Assertions.assertThrows(IllegalStateException.class, () -> service.decryptObj(hash, Object.class));
  }

}
