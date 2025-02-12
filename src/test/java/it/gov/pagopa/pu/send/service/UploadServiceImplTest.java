package it.gov.pagopa.pu.send.service;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.send.connector.client.UploadClientImpl;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

  @Mock
  private UploadClientImpl uploadClient;

  @InjectMocks
  private UploadServiceImpl uploadService;


  @Test
  void givenValidFileWhenUploadFileThenReturnsVersionId()
    throws IOException {
    //GIVEN
    String sendNotificationId = "SENDNOTIFICATIONID";
    Optional<String> versionId = Optional.of("VERSIONID");
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

    Mockito.when(uploadClient.uploadFileToS3(documentDTO, fileBytes)).thenReturn(versionId);

    Optional<String> result = uploadService.uploadFile(sendNotificationId, documentDTO);
    // THEN
    assertTrue(result.isPresent());
    assertEquals(versionId, result);
  }


  @Test
  void givenInvalidFileWhenUploadFileThenThrowsFileNotFoundException() {
      String sendNotificationId = "SENDNOTIFICATIONID";
      DocumentDTO documentDTO = DocumentDTO.builder()
              .fileName("non_existent_file.pdf")
              .build();

      assertThrows(UploadFileException.class, () -> uploadService.uploadFile(sendNotificationId, documentDTO));
  }

  @Test
  void givenInvalidSignatureWhenUploadFileThenInvalidSignatureException() throws Exception {
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

    assertThrows(UploadFileException.class, () -> {
      uploadService.uploadFile(sendNotificationId, documentDTO);
    });
  }
}
