package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.SendNotification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationMapperTest {

  private final SendNotificationMapper mapper = new SendNotificationMapper();

  @Test
  void givenCreateNotificationRequestWhenApplyThenReturnSendNotification(){
    // Given
    CreateNotificationRequest notificationRequests = CreateNotificationRequest
      .builder()
      .preloadId("TEST.pdf")
      .contentType("application/pdf")
      .sha256("ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ")
      .build();

    SendNotification sendNotification = SendNotification.builder()
      .preloadId("TEST.pdf")
      .contentType("application/pdf")
      .expectedFileDigest("ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ")
      .build();

    // When
    SendNotification result = mapper.apply(notificationRequests);

    // Then
    Assertions.assertEquals(sendNotification, result);
  }
}
