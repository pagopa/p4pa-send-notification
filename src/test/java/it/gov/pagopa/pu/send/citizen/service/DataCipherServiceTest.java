package it.gov.pagopa.pu.send.citizen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Base64;

@ExtendWith(MockitoExtension.class)
class DataCipherServiceTest {

  private DataCipherService service = new DataCipherService("PSW","PEPPER", new JsonMapper());

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
    Object mockItem = new Object();
    JsonMapper mockMapper = Mockito.mock(JsonMapper.class);

    Mockito.doThrow(JacksonIOException.construct(new IOException("DUMMY IO EXCEPTION")))
      .when(mockMapper)
      .writeValueAsString(Mockito.same(mockItem));

    service = new DataCipherService("PSW", "PEPPER", mockMapper);

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
