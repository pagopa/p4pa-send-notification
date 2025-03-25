package it.gov.pagopa.pu.send.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.util.TestUtils;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class SendNotification2SendNotificationDTOMapperTest {

  private final SendNotification2SendNotificationDTOMapper mapper = new SendNotification2SendNotificationDTOMapper();

  @Test
  void givenSendNotificationWhenMapThenReturnSendNotificationDTO() {
    LocalDate now = LocalDate.now();
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("12345");
    sendNotification.setOrganizationId(1L);
    sendNotification.setIun("IUN");
    sendNotification.setNotificationData(now);
    sendNotification.setStatus(NotificationStatus.COMPLETE);

    SendNotificationDTO result = mapper.apply(sendNotification);

    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(now, result.getNotificationDate());
    assertEquals(NotificationStatus.COMPLETE.name(), result.getStatus());
  }

}
