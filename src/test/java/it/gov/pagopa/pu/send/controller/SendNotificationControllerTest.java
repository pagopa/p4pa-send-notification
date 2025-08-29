package it.gov.pagopa.pu.send.controller;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import it.gov.pagopa.pu.send.util.SecurityUtilsTest;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendNotificationControllerTest {

  @Mock
  private SendNotificationService sendNotificationServiceMock;

  @InjectMocks
  private SendNotificationController sendNotificationController;

  private final String accessToken = "ACCESSTOKEN";

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

  @Test
  void givenNotificationRequestWithRecipientsAndPaymentsThenLogNoticeCodes() {
    // Given
    PagoPa pagoPa = new PagoPa();
    pagoPa.setNoticeCode("NOTICE123");

    Payment payment = new Payment();
    payment.setPagoPa(pagoPa);

    Recipient recipient = new Recipient();
    recipient.setPayments(List.of(payment));

    CreateNotificationRequest request = CreateNotificationRequest.builder()
      .organizationId(123L)
      .recipients(List.of(recipient))
      .build();

    CreateNotificationResponse expectedResponse = CreateNotificationResponse.builder()
      .sendNotificationId("SENDID")
      .preloadUrl("url")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    Mockito.when(sendNotificationServiceMock.createSendNotification(request, accessToken)).thenReturn(expectedResponse);

    // When
    ResponseEntity<CreateNotificationResponse> response = sendNotificationController.createSendNotification(request);

    // Then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResponse, response.getBody());
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
    Long sendNotificationId = 123L;
    String status = "ERROR";
    UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

    Mockito.when(sendNotificationServiceMock.updateNotificationStatus(String.valueOf(sendNotificationId), NotificationStatus.valueOf(status)))
      .thenReturn(updateResult);

    // When
    ResponseEntity<Void> response = sendNotificationController.updateNotificationStatus(sendNotificationId, status);

    // Then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

}
