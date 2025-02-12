package it.gov.pagopa.pu.send.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilder;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private UploadServiceImpl uploadService;


  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilder.build()).thenReturn(restTemplate);
    uploadService = new UploadServiceImpl(restTemplateBuilder);
  }

  @Test
  void givenValidFileWhenUploadFileToS3ThenReturnsVersionId() throws IOException, NoSuchAlgorithmException {
    //GIVEN
    String sendNotificationId = "SENDNOTIFICATIONID";
    String versionId = "VERSIONID";
    String filePath = "src/main/resources/tmp/" + sendNotificationId + "_file.pdf";
    File file = new File(filePath);
    file.deleteOnExit();

    try (FileWriter writer = new FileWriter(file)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName("file.pdf")
      .digest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=")
      .contentType("application/pdf")
      .httpMethod("PUT")
      .url("https://test.com/upload")
      .secret("SECRET")
      .build();

    HttpHeaders headers = new HttpHeaders();
    headers.add("x-amz-version-id", versionId);

    ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(headers).body("Success");
    Mockito.when(restTemplate.exchange(eq(URI.create(documentDTO.getUrl())), eq(
        HttpMethod.PUT), any(), eq(String.class)))
      .thenReturn(responseEntity);

    Optional<String> result = uploadService.uploadFileToS3(sendNotificationId, documentDTO);
    // THEN
    assertTrue(result.isPresent());
    assertEquals(versionId, result.get());
  }


  @Test
  void givenInvalidFileWhenUploadFileToS3ThenThrowsFileNotFoundException() {
      String sendNotificationId = "SENDNOTIFICATIONID";
      DocumentDTO documentDTO = DocumentDTO.builder()
              .fileName("non_existent_file.pdf")
              .build();

      assertThrows(IOException.class, () -> uploadService.uploadFileToS3(sendNotificationId, documentDTO));
  }

  @Test
  void givenInvalidSignatureWhenUploadFileToS3ThenInvalidSignatureException() throws Exception {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String filePath = "src/main/resources/tmp/" + sendNotificationId + "_file.pdf";
    File file = new File(filePath);
    file.deleteOnExit();

    try (FileWriter writer = new FileWriter(file)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }

    DocumentDTO documentDTO = new DocumentDTO();
    documentDTO.setFileName("file.pdf");
    documentDTO.setDigest("invalidDigest");

    assertThrows(InvalidSignatureException.class, () -> {
      uploadService.uploadFileToS3(sendNotificationId, documentDTO);
    });
  }

  @Test
  void givenRestTemplateErrorWhenUploadFileToS3ThenThrowsUploadFileException()
    throws IOException {
    String sendNotificationId = "SENDNOTIFICATIONID";
    String filePath = "src/main/resources/tmp/" + sendNotificationId + "_file.pdf";
    File file = new File(filePath);
    file.deleteOnExit();

    try (FileWriter writer = new FileWriter(file)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }
      DocumentDTO documentDTO = DocumentDTO.builder()
              .fileName("file.pdf")
              .digest("9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=")
              .contentType("application/pdf")
              .httpMethod("PUT")
              .url("https://test.com/upload")
              .secret("SECRET")
              .build();

      Mockito.when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(), eq(String.class)))
              .thenThrow(new RuntimeException("RestTemplate error"));

      assertThrows(UploadFileException.class, () -> uploadService.uploadFileToS3(sendNotificationId, documentDTO));
  }

}
