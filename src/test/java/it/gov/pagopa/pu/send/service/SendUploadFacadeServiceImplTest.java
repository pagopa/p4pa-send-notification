package it.gov.pagopa.pu.send.service;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.UploadFileException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendUploadFacadeServiceImplTest {

  @Mock
  private SendUploadClient sendUploadClient;

  @Mock
  private FileStorerService fileStorerServiceMock;

  @InjectMocks
  private SendUploadFacadeServiceImpl uploadService;

  @Test
  void givenValidFileWhenUploadFileThenReturnsVersionId() {
    //GIVEN
    String sendNotificationId = "sendNotificationId";
    Optional<String> versionId = Optional.of("VERSIONID");
    Long organizationId = 1L;
    InputStream inputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    DocumentDTO documentDTO = DocumentDTO.builder()
      .fileName("file.pdf")
      .digest("YSxsCpvZHvwL8IIosWJBUDjgUwa01sBHu6Cj4laQRLA=")
      .contentType("application/pdf")
      .httpMethod("PUT")
      .url("https://test.com/upload")
      .secret("SECRET")
      .build();

    Mockito.when(fileStorerServiceMock.retrieveFile(organizationId, sendNotificationId,sendNotificationId+"_file.pdf")).thenReturn(inputStream);
    Mockito.when(sendUploadClient.upload(documentDTO, "TEST FILE HASH P4PA SEND".getBytes())).thenReturn(versionId);

    Optional<String> result = uploadService.uploadFile(organizationId, sendNotificationId, documentDTO);
    // THEN
    assertTrue(result.isPresent());
    assertEquals(versionId, result);
  }

  @Test
  void givenInvalidFileWhenUploadFileThenThrowsFileNotFoundException() {
      String sendNotificationId = "SENDNOTIFICATIONID";
      Long organizationId = 1L;

      DocumentDTO documentDTO = DocumentDTO.builder()
              .fileName("non_existent_file.pdf")
              .build();

      assertThrows(UploadFileException.class, () -> uploadService.uploadFile(organizationId, sendNotificationId, documentDTO));
  }

}
