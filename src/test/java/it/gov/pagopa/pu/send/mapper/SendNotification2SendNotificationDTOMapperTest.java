package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class SendNotification2SendNotificationDTOMapperTest {

  private final SendNotification2SendNotificationDTOMapper mapper = new SendNotification2SendNotificationDTOMapper();

  @Test
  void givenSendNotificationWhenMapThenReturnSendNotificationDTO() {
    OffsetDateTime now = OffsetDateTime.now();
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("12345");
    sendNotification.setOrganizationId(1L);
    sendNotification.setIun("IUN");
    sendNotification.setNotificationData(now);
    sendNotification.setStatus(NotificationStatus.COMPLETE);

    sendNotification.setPayments(List.of(
      new PuPayment(3L, new Payment(PagoPa.builder()
        .noticeCode("NOTICECODE1")
        .build())),
      new PuPayment(3L, new Payment(PagoPa.builder()
        .noticeCode("NOTICECODE2")
        .build())),
      new PuPayment(4L, new Payment(PagoPa.builder()
        .noticeCode("NOTICECODE3")
        .build()))
    ));

    SendNotificationDTO result = mapper.apply(sendNotification);

    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(now, result.getNotificationDate());
    assertEquals(NotificationStatus.COMPLETE.name(), result.getStatus());
    assertEquals(
      List.of(
        new SendNotificationPaymentsDTO(3L, List.of("NOTICECODE1", "NOTICECODE2")),
        new SendNotificationPaymentsDTO(4L, List.of("NOTICECODE3"))
      ),
      result.getPayments());
  }

}
