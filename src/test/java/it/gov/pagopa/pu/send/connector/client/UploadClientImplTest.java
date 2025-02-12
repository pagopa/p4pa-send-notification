package it.gov.pagopa.pu.send.connector.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class UploadClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;
  @Mock
  private RestTemplate restTemplateMock;

  private UploadClientImpl uploadClient;

  @BeforeEach
  public void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    uploadClient = new UploadClientImpl(restTemplateBuilderMock);
  }

  @Test
  void givenValidFileWhenUploadFileToS3ThenReturnsVersionId() throws IOException {
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

    byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

    HttpHeaders headers = new HttpHeaders();
    headers.add("x-amz-version-id", versionId);

    ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(headers).body("Success");
    Mockito.when(restTemplateMock.exchange(eq(URI.create(documentDTO.getUrl())), eq(
        HttpMethod.PUT), any(), eq(String.class)))
      .thenReturn(responseEntity);

    Optional<String> result = uploadClient.uploadFileToS3(documentDTO, fileBytes);
    // THEN
    assertTrue(result.isPresent());
    assertEquals(versionId, result.get());
  }
}
