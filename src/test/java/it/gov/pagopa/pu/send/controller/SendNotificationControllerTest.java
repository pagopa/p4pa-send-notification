package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import java.util.List;
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
    List<CreateNotificationRequest> request = List.of(CreateNotificationRequest
      .builder()
      .preloadId("TEST.pdf")
      .contentType("application/pdf")
      .sha256("ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ")
      .build());

    CreateNotificationResponse expectedResponse = CreateNotificationResponse.builder()
      .sendNotificationId(1L)
      .preloadRef(null)
      .status(NotificationStatus.WAITING_FILE.name())
      .build();

    Mockito.when(sendNotificationServiceMock.createSendNotification(request)).thenReturn(expectedResponse);

    ResponseEntity<CreateNotificationResponse> response = sendNotificationController.createSendNotification(request);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(expectedResponse, response.getBody());
  }
}
