package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.dto.generated.StartNotificationResponse;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SendNotificationControllerTest {

  @Mock
  private SendNotificationService sendNotificationServiceMock;

  @InjectMocks
  private SendNotificationController sendNotificationController;

  @Test
  void givenValidNotificationRequestThenOk(){
    // Given
    Long organizationId = 1L;
    CreateNotificationRequest request = CreateNotificationRequest.builder().build();
    CreateNotificationResponse expectedResponse = CreateNotificationResponse.builder()
      .sendNotificationId("SENDNOTIFICATIONID")
      .preloadUrl("PRELOADURL")
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    // When
    Mockito.when(sendNotificationServiceMock.createSendNotification(request, organizationId)).thenReturn(expectedResponse);

    // Then
    ResponseEntity<CreateNotificationResponse> response = sendNotificationController.createSendNotification(organizationId, request);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void givenStartNotificationRequestThenOk(){
    // Given
    Long organizationId = 1L;
    String sendNotificationId = "SENDNOTIFICATIONID";
    LoadFileRequest loadFileRequest = LoadFileRequest.builder()
      .fileName("FILENAME")
      .digest("DIGEST")
      .build();

    StartNotificationResponse expectedResponse = StartNotificationResponse.builder().workFlowId(sendNotificationId).build();

    // When
    Mockito.when(sendNotificationServiceMock.startSendNotification(sendNotificationId, organizationId, loadFileRequest))
      .thenReturn(expectedResponse);

    // Then
    ResponseEntity<StartNotificationResponse> response = sendNotificationController.startNotification(sendNotificationId, organizationId, loadFileRequest);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void givenStartNotificationRequestThenAccepted(){
    // Given
    Long organizationId = 1L;
    String sendNotificationId = "SENDNOTIFICATIONID";
    LoadFileRequest loadFileRequest = LoadFileRequest.builder()
      .fileName("FILENAME")
      .digest("DIGEST")
      .build();

    // When
    Mockito.when(sendNotificationServiceMock.startSendNotification(sendNotificationId, organizationId, loadFileRequest))
      .thenReturn(null);

    // Then
    ResponseEntity<StartNotificationResponse> response = sendNotificationController.startNotification(sendNotificationId, organizationId, loadFileRequest);
    Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
  }

  @Test
  void givenValidSendNotificationIdWhenDeleteNotificationThenOk(){
    //Given
    String sendNotificationId = "SENDNOTIFICATIONID";
    Long organizationId = 1L;
    // When
    Mockito.doNothing().when(sendNotificationServiceMock).deleteSendNotification(sendNotificationId, organizationId);
    //Then
    ResponseEntity<Void> response = sendNotificationController.deleteSendNotification(sendNotificationId, organizationId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

}
