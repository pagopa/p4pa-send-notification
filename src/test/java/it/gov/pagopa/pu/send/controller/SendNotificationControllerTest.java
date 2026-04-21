package it.gov.pagopa.pu.send.controller;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import it.gov.pagopa.pu.send.util.SecurityUtilsTest;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SendNotificationControllerTest {

  @Mock
  private SendNotificationService sendNotificationServiceMock;

  @InjectMocks
  private SendNotificationController sendNotificationController;

  private final String accessToken = "ACCESSTOKEN";
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void clearContext(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void givenValidNotificationRequestThenOk(){
    // Given
    CreateNotificationRequest request = CreateNotificationRequest.builder().build();
    CreateNotificationResponse expectedResponse = CreateNotificationResponse.builder()
      .sendNotificationId("SENDNOTIFICATIONID")
      .preloadUrl("PRELOADURL")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    // When
    Mockito.when(sendNotificationServiceMock.createSendNotification(request, accessToken)).thenReturn(expectedResponse);

    // Then
    ResponseEntity<CreateNotificationResponse> response = sendNotificationController.createSendNotification(request);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void givenStartNotificationRequestThenOk(){
    // Given
    String sendNotificationId = "SENDNOTIFICATIONID";
    LoadFileRequest loadFileRequest = LoadFileRequest.builder()
      .fileName("FILENAME")
      .digest("DIGEST")
      .build();

    StartNotificationResponse expectedResponse = new StartNotificationResponse();

    Mockito.when(sendNotificationServiceMock.startSendNotification(sendNotificationId, loadFileRequest, accessToken))
      .thenReturn(expectedResponse);

    // When
    ResponseEntity<StartNotificationResponse> response = sendNotificationController.startNotification(sendNotificationId, loadFileRequest);

    // Then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResponse, response.getBody());
  }

  @Test
  void givenStartNotificationRequestThenAccepted(){
    // Given
    String sendNotificationId = "SENDNOTIFICATIONID";
    LoadFileRequest loadFileRequest = LoadFileRequest.builder()
      .fileName("FILENAME")
      .digest("DIGEST")
      .build();

    Mockito.when(sendNotificationServiceMock.startSendNotification(sendNotificationId, loadFileRequest, accessToken))
      .thenReturn(null);

    // When
    ResponseEntity<StartNotificationResponse> response = sendNotificationController.startNotification(sendNotificationId, loadFileRequest);

    // Then
    Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
  }

  @Test
  void givenValidSendNotificationIdWhenDeleteNotificationThenOk(){
    //Given
    String sendNotificationId = "SENDNOTIFICATIONID";
    // When
    Mockito.doNothing().when(sendNotificationServiceMock).deleteSendNotification(sendNotificationId);
    //Then
    ResponseEntity<Void> response = sendNotificationController.deleteSendNotification(sendNotificationId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenValidNotificationRequestIdWhenGetSendNotificationThenOk(){
    //Given
    String notificationRequestId = "NOTIFICATION_REQUEST_ID";
    SendNotificationDTO expectedResult = new SendNotificationDTO();
    // When
    Mockito.when(sendNotificationServiceMock.findSendNotificationDTOByNotificationRequestId(notificationRequestId))
      .thenReturn(expectedResult);
    //Then
    ResponseEntity<SendNotificationDTO> response = sendNotificationController.getSendNotificationByNotificationRequestId(notificationRequestId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResult, response.getBody());
  }

  @Test
  void whenGetSendNotificationThenInvokeService(){
    //Given
    String sendNotificationId = "SENDNOTIFICATIONID";
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    Mockito.when(sendNotificationServiceMock.findSendNotificationDTO(sendNotificationId))
      .thenReturn(expectedResult);

    // When
    //Then
    ResponseEntity<SendNotificationDTO> response = sendNotificationController.getSendNotification(sendNotificationId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResult, response.getBody());
  }

  @ParameterizedTest
  @MethodSource("provideNotificationRequests")
  void givenNotificationRequestWithDifferentPaymentsThenLogCorrectly(
    CreateNotificationRequest request,
    CreateNotificationResponse expectedResponse
  ) {
    // Given
    Mockito.when(sendNotificationServiceMock.createSendNotification(request, accessToken))
      .thenReturn(expectedResponse);

    // When
    ResponseEntity<CreateNotificationResponse> response =
      sendNotificationController.createSendNotification(request);

    // Then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResponse, response.getBody());
  }

  private static Stream<Arguments> provideNotificationRequests() {
    // PagoPa only
    PagoPa pagoPa = new PagoPa();
    pagoPa.setNoticeCode("NOTICE123");
    Payment pagoPaPayment = new Payment();
    pagoPaPayment.setPagoPa(pagoPa);
    Recipient pagoPaRecipient = new Recipient();
    pagoPaRecipient.setPayments(List.of(pagoPaPayment));
    CreateNotificationRequest requestPagoPa = CreateNotificationRequest.builder()
      .organizationId(1L)
      .recipients(List.of(pagoPaRecipient))
      .build();
    CreateNotificationResponse respPagoPa = CreateNotificationResponse.builder()
      .sendNotificationId("SENDID_PAGOPA")
      .preloadUrl("url")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    // F24 only
    F24Payment f24 = new F24Payment();
    f24.setTitle("F24TITLE");
    Payment f24Payment = new Payment();
    f24Payment.setF24(f24);
    Recipient f24Recipient = new Recipient();
    f24Recipient.setPayments(List.of(f24Payment));
    CreateNotificationRequest requestF24 = CreateNotificationRequest.builder()
      .organizationId(2L)
      .recipients(List.of(f24Recipient))
      .build();
    CreateNotificationResponse respF24 = CreateNotificationResponse.builder()
      .sendNotificationId("SENDID_F24")
      .preloadUrl("url")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    // PagoPa + F24
    Payment bothPayment = new Payment();
    bothPayment.setPagoPa(pagoPa);
    bothPayment.setF24(f24);
    Recipient bothRecipient = new Recipient();
    bothRecipient.setPayments(List.of(bothPayment));
    CreateNotificationRequest requestBoth = CreateNotificationRequest.builder()
      .organizationId(3L)
      .recipients(List.of(bothRecipient))
      .build();
    CreateNotificationResponse respBoth = CreateNotificationResponse.builder()
      .sendNotificationId("SENDID_BOTH")
      .preloadUrl("url")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    // No payments
    Recipient noPaymentRecipient = new Recipient();
    CreateNotificationRequest requestNoPay = CreateNotificationRequest.builder()
      .organizationId(4L)
      .recipients(List.of(noPaymentRecipient))
      .build();
    CreateNotificationResponse respNoPay = CreateNotificationResponse.builder()
      .sendNotificationId("SENDID_NOPAY")
      .preloadUrl("url")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    // Empty values
    PagoPa emptyPagoPa = new PagoPa();
    emptyPagoPa.setNoticeCode("");
    F24Payment emptyF24 = new F24Payment();
    emptyF24.setTitle("");
    Payment emptyPayment = new Payment();
    emptyPayment.setPagoPa(emptyPagoPa);
    emptyPayment.setF24(emptyF24);
    Recipient emptyRecipient = new Recipient();
    emptyRecipient.setPayments(List.of(emptyPayment));
    CreateNotificationRequest requestEmpty = CreateNotificationRequest.builder()
      .organizationId(5L)
      .recipients(List.of(emptyRecipient))
      .build();
    CreateNotificationResponse respEmpty = CreateNotificationResponse.builder()
      .sendNotificationId("SENDID_EMPTY")
      .preloadUrl("url")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    return Stream.of(
      Arguments.of(requestPagoPa, respPagoPa),
      Arguments.of(requestF24, respF24),
      Arguments.of(requestBoth, respBoth),
      Arguments.of(requestNoPay, respNoPay),
      Arguments.of(requestEmpty, respEmpty)
    );
  }

  @Test
  void whenFindSendNotificationByOrgIdAndNavThenInvokeService(){
    //Given
    Long organizationId = 1L;
    String nav = "NAV";
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    Mockito.when(sendNotificationServiceMock.findSendNotificationByOrgIdAndNav(organizationId, nav))
      .thenReturn(expectedResult);

    // When
    //Then
    ResponseEntity<SendNotificationDTO> response = sendNotificationController.findSendNotificationByOrgIdAndNav(organizationId, nav);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResult, response.getBody());
  }

  @Test
  void whenUpdateNotificationStatusThenReturnOk() {
    // Given
    String notificationRequestId = "REQUESTID";
    UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

    Mockito.when(sendNotificationServiceMock.updateNotificationStatus(notificationRequestId, NotificationStatus.REFUSED))
      .thenReturn(updateResult);

    // When
    ResponseEntity<Void> response = sendNotificationController.updateNotificationStatus(notificationRequestId, NotificationStatus.REFUSED);

    // Then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenGetLegalFactsThenOk(){
    // Given
    String sendNotificationId = "SENDNOTIFICATIONID";
    LegalFactDTO legalFactDTO = LegalFactDTO.builder()
      .fileName("NAME")
      .category(LegalFactCategoryDTO.SENDER_ACK)
      .url("URL")
      .build();

    Mockito.when(sendNotificationServiceMock.getLegalFacts(sendNotificationId)).thenReturn(List.of(legalFactDTO));

    // When
    ResponseEntity<List<LegalFactDTO>> response = sendNotificationController.getLegalFacts(sendNotificationId);

    // Then
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(List.of(legalFactDTO), response.getBody());
  }

  @Test
  void whenDeleteExpiredLegalFactsThenOk(){
    String sendNotificationId = "SENDNOTIFICATIONID";
    OffsetDateTime scheduledDateTime = OffsetDateTime.now();
    FileExpirationResponseDTO expectedResponse = podamFactory.manufacturePojo(FileExpirationResponseDTO.class);

    Mockito.when(sendNotificationServiceMock.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime, accessToken)).thenReturn(expectedResponse);

    ResponseEntity<FileExpirationResponseDTO> response = sendNotificationController.deleteExpiredLegalFacts(sendNotificationId, scheduledDateTime);

    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }
}
