package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.enums.Status;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
    CreateNotificationRequest request = CreateNotificationRequest.builder().build();
    CreateNotificationResponse expectedResponse = CreateNotificationResponse.builder()
      .sendNotificationId("SENDNOTIFICATIONID")
      .preloadRef(null) //TODO P4ADEV-2080 comunicate fileName and url for upload file
      .status(Status.WAITING_FILE.name())
      .build();

    // When
    Mockito.when(sendNotificationServiceMock.createSendNotification(request)).thenReturn(expectedResponse);

    // Then
    ResponseEntity<CreateNotificationResponse> response = sendNotificationController.createSendNotification(request);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(expectedResponse, response.getBody());
  }
}
