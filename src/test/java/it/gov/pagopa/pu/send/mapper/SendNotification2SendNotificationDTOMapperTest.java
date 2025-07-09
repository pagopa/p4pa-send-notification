package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
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
    PuPayment payment1 = new PuPayment(3L, new Payment(PagoPa.builder().noticeCode("NOTICECODE1").build()), OffsetDateTime.now());
    PuPayment payment2 = new PuPayment(3L, new Payment(PagoPa.builder().noticeCode("NOTICECODE2").build()), OffsetDateTime.now());
    PuPayment payment3 = new PuPayment(4L, new Payment(PagoPa.builder().noticeCode("NOTICECODE3").build()), OffsetDateTime.now());

    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO();
    recipient.setPuPayments(List.of(payment1, payment2, payment3));

    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();
    sendNotificationNoPII.setSendNotificationId("12345");
    sendNotificationNoPII.setOrganizationId(1L);
    sendNotificationNoPII.setIun("IUN");
    sendNotificationNoPII.setStatus(NotificationStatus.COMPLETE);
    sendNotificationNoPII.setRecipients(List.of(recipient));

    // when
    SendNotificationDTO result = mapper.apply(sendNotificationNoPII);

    // then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(NotificationStatus.COMPLETE, result.getStatus());

    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(3L, List.of("NOTICECODE1", "NOTICECODE2")),
      new SendNotificationPaymentsDTO(4L, List.of("NOTICECODE3"))
    );
    assertEquals(expectedPayments, result.getPayments());
  }
}
