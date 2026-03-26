package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class SendNotification2SendNotificationDTOMapperTest {

  private final SendNotification2SendNotificationDTOMapper mapper = new SendNotification2SendNotificationDTOMapper();

  @Test
  void givenPagoPaPaymentSendNotificationWhenMapThenReturnSendNotificationDTO() {
    OffsetDateTime now = OffsetDateTime.now();
    PuPayment puPayment1 = new PuPayment(1L, new Payment(PagoPa.builder().noticeCode("NOTICECODE1").build(), null), now);
    PuPayment puPayment2 = new PuPayment(1L, new Payment(PagoPa.builder().noticeCode("NOTICECODE2").build(), null), now);
    PuPayment puPayment3 = new PuPayment(2L, new Payment(PagoPa.builder().noticeCode("NOTICECODE3").build(), null), now);
    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment1, puPayment2, puPayment3));

    // when
    SendNotificationDTO result = mapper.apply(sendNotificationNoPII);

    // then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(NotificationStatus.IN_VALIDATION, result.getStatus());

    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(1L, List.of("NOTICECODE1", "NOTICECODE2"), now),
      new SendNotificationPaymentsDTO(2L, List.of("NOTICECODE3"), now)
    );
    assertEquals(expectedPayments, result.getPayments());
  }

  @Test
  void givenF24PaymentSendNotificationWhenMapThenReturnSendNotificationDTO() {
    OffsetDateTime now = OffsetDateTime.now();
    PuPayment puPayment = new PuPayment(1L, new Payment(null,  F24Payment.builder().title("F24").build()), now);
    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment));

    // when
    SendNotificationDTO result = mapper.apply(sendNotificationNoPII);

    // then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(NotificationStatus.IN_VALIDATION, result.getStatus());

    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(1L, Collections.emptyList(), now)
    );
    assertEquals(expectedPayments, result.getPayments());
  }

  @Test
  void givenBothPagoPaAndF24PaymentSendNotificationWhenMapThenReturnSendNotificationDTO() {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = new Payment(
      PagoPa.builder().noticeCode("NOTICECODE").build(),
      F24Payment.builder().title("F24").build()
    );
    PuPayment puPayment = new PuPayment(1L, payment, now);
    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment));

    // when
    SendNotificationDTO result = mapper.apply(sendNotificationNoPII);

    // then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(NotificationStatus.IN_VALIDATION, result.getStatus());

    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(1L, List.of("NOTICECODE"), now)
    );
    assertEquals(expectedPayments, result.getPayments());
  }

  @Test
  void givenPagoPaPaymentWithoutDebtPositionIdSendNotificationWhenMapThenReturnSendNotificationDTO() {
    OffsetDateTime now = OffsetDateTime.now();
    PuPayment puPayment = new PuPayment(null, new Payment(PagoPa.builder().noticeCode("NOTICECODE").build(), null), now);
    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment));

    // when
    SendNotificationDTO result = mapper.apply(sendNotificationNoPII);

    // then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals("12345", result.getSendNotificationId());
    assertEquals(1L, result.getOrganizationId());
    assertEquals("IUN", result.getIun());
    assertEquals(NotificationStatus.IN_VALIDATION, result.getStatus());

    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(null, List.of("NOTICECODE"), now)
    );
    assertEquals(expectedPayments, result.getPayments());
  }


  private static SendNotificationNoPII createSendNotificationNoPII(List<PuPayment> puPayments) {
    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO();
    recipient.setPuPayments(puPayments);

    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();
    sendNotificationNoPII.setSendNotificationId("12345");
    sendNotificationNoPII.setOrganizationId(1L);
    sendNotificationNoPII.setIun("IUN");
    sendNotificationNoPII.setStatus(NotificationStatus.IN_VALIDATION);
    sendNotificationNoPII.setRecipients(List.of(recipient));
    return sendNotificationNoPII;
  }

}
