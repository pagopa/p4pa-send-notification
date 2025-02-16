package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.service.SendFacadeService;
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
class SendControllerTest {

  @Mock
  private SendFacadeService sendFacadeServiceMock;

  @InjectMocks
  private SendController sendController;

  @Test
  void givenSendNotificationIdWhenPreloadFilesRequestThenOk() {
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).preloadFiles(sendNotificationId);
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
    Mockito.doNothing().when(sendFacadeServiceMock).deliveryNotification(sendNotificationId);
    ResponseEntity<Void> response = sendController.deliveryNotification(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
