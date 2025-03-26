package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
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

  @Test
  void givenSendNotificationIdWhenNotificationStatusRequestThenOk(){
    String sendNotificationId = "12345";
    SendNotificationDTO status = new SendNotificationDTO();
    Mockito.when(sendFacadeServiceMock.notificationStatus(sendNotificationId)).thenReturn(status);

    ResponseEntity<SendNotificationDTO> response = sendController.notificationStatus(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(status, response.getBody());
  }

  @Test
  void givenSendNotificationIdAndOrganizationIdWhenRetrieveNotificationDateThenOk() {
    String sendNotificationId = "12345";

    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    Mockito.when(sendFacadeServiceMock.retrieveNotificationData(sendNotificationId))
      .thenReturn(notificationDTO);

    ResponseEntity<SendNotificationDTO> response = sendController.retrieveNotificationDate(sendNotificationId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdAndOrganizationIdWhenRetrieveNotificationDateThenNoContent() {
    String sendNotificationId = "12345";
    Mockito.when(sendFacadeServiceMock.retrieveNotificationData(sendNotificationId))
      .thenReturn(null);

    ResponseEntity<SendNotificationDTO> response = sendController.retrieveNotificationDate(sendNotificationId);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }
}
