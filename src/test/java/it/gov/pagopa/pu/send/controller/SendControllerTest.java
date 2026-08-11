package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.generated.LegalFactDownloadMetadataDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.service.SendFacadeService;
import it.gov.pagopa.pu.send.util.SecurityUtilsTest;
import it.gov.pagopa.send.dto.generated.LegalFactCategoryDTO;
import it.gov.pagopa.send.dto.generated.NotificationPriceResponseV23DTO;
import it.gov.pagopa.send.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.send.dto.generated.TimelineElementCategoryV27DTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendControllerTest {

  @Mock
  private SendFacadeService sendFacadeServiceMock;

  @InjectMocks
  private SendController sendController;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, "MAPPEDEXTERNALUSERID");
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
    Mockito.verifyNoMoreInteractions(
      sendFacadeServiceMock
    );
  }

  @Test
  void givenSendNotificationIdWhenPreloadFilesRequestThenOk() {
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).preloadFiles(sendNotificationId, accessToken);
    ResponseEntity<Void> response = sendController.preloadSendFile(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenUploadFilesRequestThenOk(){
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).uploadFiles(sendNotificationId);
    ResponseEntity<Void> response = sendController.uploadSendFile(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenDeliveryNotificationRequestThenOk(){
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).deliveryNotification(sendNotificationId, accessToken);
    ResponseEntity<Void> response = sendController.deliveryNotification(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenNotificationStatusRequestThenOk(){
    String sendNotificationId = "12345";
    SendNotificationDTO status = new SendNotificationDTO();
    when(sendFacadeServiceMock.notificationStatus(sendNotificationId, accessToken)).thenReturn(status);

    ResponseEntity<SendNotificationDTO> response = sendController.notificationStatus(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(status, response.getBody());
  }

  @Test
  void givenSendNotificationIdAndOrganizationIdWhenRetrieveNotificationDateThenOk() {
    String sendNotificationId = "12345";

    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    when(sendFacadeServiceMock.retrieveNotificationDate(sendNotificationId, accessToken))
      .thenReturn(notificationDTO);

    ResponseEntity<SendNotificationDTO> response = sendController.retrieveNotificationDate(sendNotificationId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdAndOrganizationIdWhenRetrieveNotificationDateThenNoContent() {
    String sendNotificationId = "12345";
    when(sendFacadeServiceMock.retrieveNotificationDate(sendNotificationId, accessToken))
      .thenReturn(null);

    ResponseEntity<SendNotificationDTO> response = sendController.retrieveNotificationDate(sendNotificationId);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void givenOrganizationIdAndNavWhenRetrieveNotificationPriceRequestThenOk(){
    Long organizationId = 1L;
    String nav = "12345";
    NotificationPriceResponseV23DTO price = new NotificationPriceResponseV23DTO();
    when(sendFacadeServiceMock.retrieveNotificationPrice(organizationId, nav, accessToken))
      .thenReturn(price);

    ResponseEntity<NotificationPriceResponseV23DTO> response = sendController.retrieveNotificationPrice(organizationId, nav);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(price, response.getBody());
  }

  @Test
  void givenSendNotificationIdWhenRetrieveLegalFactsThenOk() {
    // GIVEN
    String sendNotificationId = "12345";

    List<LegalFactListElementDTO> expectedLegalFacts = new ArrayList<>();
    when(sendFacadeServiceMock.retrieveLegalFacts(sendNotificationId, accessToken))
      .thenReturn(expectedLegalFacts);

    // WHEN
    ResponseEntity<List<LegalFactListElementDTO>> actualLegalFactsResponse = sendController.retrieveLegalFacts(sendNotificationId);

    // THEN
    Assertions.assertNotNull(actualLegalFactsResponse);
    Assertions.assertEquals(expectedLegalFacts, actualLegalFactsResponse.getBody());
    Assertions.assertEquals(HttpStatus.OK, actualLegalFactsResponse.getStatusCode());
  }

  @Test
  void givenSendNotificationIdAndLegalFactIdWhenRetrieveLegalFactDownloadMetadataThenOk() {
    // GIVEN
    String sendNotificationId = "12345";
    String legalFactId = "12345";

    LegalFactDownloadMetadataDTO mockedResponse = new LegalFactDownloadMetadataDTO();
    when(sendFacadeServiceMock.retrieveLegalFactDownloadMetadata(sendNotificationId, legalFactId, accessToken))
      .thenReturn(mockedResponse);

    // WHEN
    ResponseEntity<LegalFactDownloadMetadataDTO> actualResponse = sendController.retrieveLegalFactDownloadMetadata(sendNotificationId, legalFactId);

    // THEN
    Assertions.assertNotNull(actualResponse);
    Assertions.assertEquals(mockedResponse, actualResponse.getBody());
    Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
  }

  @Test
  void givenValidRequestWhenUploadLegalFactThenOk() throws IOException {
    //Given
    String notificationRequestId = "notificationRequestId";
    String fileName = "test.txt";

    Mockito.doNothing()
      .when(sendFacadeServiceMock)
      .downloadAndArchiveSendLegalFact(
        notificationRequestId,
        LegalFactCategoryDTO.SENDER_ACK,
        fileName,
        accessToken
      );

    // When
    ResponseEntity<Void> response = sendController.downloadAndArchiveSendLegalFact(notificationRequestId, LegalFactCategoryDTO.SENDER_ACK, fileName);

    //Then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenIOExceptionIsThrownWhenUploadLegalFactThenThrowHttpServerErrorException() throws IOException {
    //Given
    String notificationRequestId = "notificationRequestId";
    String fileName = "test.txt";

    doThrow(new IOException("IO error message"))
      .when(sendFacadeServiceMock)
      .downloadAndArchiveSendLegalFact(
        notificationRequestId,
        LegalFactCategoryDTO.SENDER_ACK,
        fileName,
        accessToken
      );

    // When
    HttpServerErrorException httpServerErrorException = Assertions.assertThrows(
      HttpServerErrorException.class,
      () -> sendController.downloadAndArchiveSendLegalFact(
        notificationRequestId,
        LegalFactCategoryDTO.SENDER_ACK,
        fileName
      )
    );

    //Then
    Assertions.assertNotNull(httpServerErrorException);
    Assertions.assertEquals(
      "500 IO error message",
      httpServerErrorException.getMessage()
    );
  }

  @Test
  void givenExpectedMapsWhenNotifySendNotificationTimelineCategoryThenOk() {
    // GIVEN
    StreamEventSummaryDTO ev1 = new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.REQUEST_ACCEPTED);
    StreamEventSummaryDTO ev2 = new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS);

    Map<String, List<StreamEventSummaryDTO>> request = Map.of(
      "notificationRequestId1", List.of(ev1),
      "notificationRequestId2", List.of(ev1,ev2)
    );

    doNothing()
      .when(sendFacadeServiceMock)
      .notifySendNotificationStreamEvents(request);

    // WHEN
    ResponseEntity<Void> actualResponse = sendController.notifySendNotificationStreamEvents(request);

    // THEN
    Assertions.assertNotNull(actualResponse);
    Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
  }
}
