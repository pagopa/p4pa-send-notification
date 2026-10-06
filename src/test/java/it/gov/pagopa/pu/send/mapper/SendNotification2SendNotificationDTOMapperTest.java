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

  private final SendNotification2SendNotificationDTOMapper mapper =
    new SendNotification2SendNotificationDTOMapper();

  @Test
  void givenPagoPaPaymentSendNotificationWhenMapThenReturnSendNotificationDTO() {
    // given
    OffsetDateTime now = OffsetDateTime.now();

    PuPayment puPayment1 = PuPayment.builder()
      .debtPositionId(1L)
      .payment(new Payment(PagoPa.builder().noticeCode("NOTICECODE1").build(), null))
      .notificationDate(now)
      .build();

    PuPayment puPayment2 = PuPayment.builder()
      .debtPositionId(1L)
      .payment(new Payment(PagoPa.builder().noticeCode("NOTICECODE2").build(), null))
      .notificationDate(now)
      .build();

    PuPayment puPayment3 = PuPayment.builder()
      .debtPositionId(2L)
      .payment(new Payment(PagoPa.builder().noticeCode("NOTICECODE3").build(), null))
      .notificationDate(now)
      .build();

    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment1, puPayment2, puPayment3));

    // when
    SendNotificationDTO sendNotificationDTO = mapper.mapToSendNotificationDTO(sendNotificationNoPII);

    // then
    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(1L, List.of("NOTICECODE1", "NOTICECODE2"), now),
      new SendNotificationPaymentsDTO(2L, List.of("NOTICECODE3"), now)
    );

    assertMappedSendNotificationDTO(sendNotificationDTO, sendNotificationNoPII, expectedPayments);
  }

  @Test
  void givenF24PaymentSendNotificationWhenMapThenReturnSendNotificationDTO() {
    // given
    OffsetDateTime now = OffsetDateTime.now();

    PuPayment puPayment = PuPayment.builder()
      .debtPositionId(1L)
      .payment(new Payment(null, F24Payment.builder().title("F24").build()))
      .notificationDate(now)
      .build();

    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment));

    // when
    SendNotificationDTO sendNotificationDTO = mapper.mapToSendNotificationDTO(sendNotificationNoPII);

    // then
    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(1L, Collections.emptyList(), now)
    );

    assertMappedSendNotificationDTO(sendNotificationDTO, sendNotificationNoPII, expectedPayments);
  }

  @Test
  void givenBothPagoPaAndF24PaymentSendNotificationWhenMapThenReturnSendNotificationDTO() {
    // given
    OffsetDateTime now = OffsetDateTime.now();

    Payment payment = new Payment(
      PagoPa.builder()
        .noticeCode("NOTICECODE")
        .build(),
      F24Payment.builder()
        .title("F24")
        .build()
    );

    PuPayment puPayment = PuPayment.builder()
      .debtPositionId(1L)
      .payment(payment)
      .notificationDate(now)
      .build();

    SendNotificationNoPII sendNotificationNoPII = createSendNotificationNoPII(List.of(puPayment));

    // when
    SendNotificationDTO sendNotificationDTO = mapper.mapToSendNotificationDTO(sendNotificationNoPII);

    // then
    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(1L, List.of("NOTICECODE"), now)
    );

    assertMappedSendNotificationDTO(sendNotificationDTO, sendNotificationNoPII, expectedPayments);
  }

  @Test
  void givenPagoPaPaymentWithoutDebtPositionIdSendNotificationWhenMapThenReturnSendNotificationDTO() {
    // given
    OffsetDateTime now = OffsetDateTime.now();

    PuPayment puPayment = PuPayment.builder()
      .payment(new Payment(
        PagoPa.builder().noticeCode("NOTICECODE").build(),
        null
      ))
      .notificationDate(now)
      .build();

    SendNotificationNoPII sendNotificationNoPII =
      createSendNotificationNoPII(List.of(puPayment));

    // when
    SendNotificationDTO sendNotificationDTO =
      mapper.mapToSendNotificationDTO(sendNotificationNoPII);

    // then
    List<SendNotificationPaymentsDTO> expectedPayments = List.of(
      new SendNotificationPaymentsDTO(
        null,
        List.of("NOTICECODE"),
        now
      )
    );

    assertMappedSendNotificationDTO(sendNotificationDTO, sendNotificationNoPII, expectedPayments);
  }

  private void assertMappedSendNotificationDTO(SendNotificationDTO mappedSendNotificationDTO, SendNotificationNoPII sendNotificationNoPII, List<SendNotificationPaymentsDTO> expectedPayments) {
    TestUtils.checkNotNullFields(mappedSendNotificationDTO);

    assertNotNull(mappedSendNotificationDTO);
    assertEquals(sendNotificationNoPII.getSendNotificationId(), mappedSendNotificationDTO.getSendNotificationId());
    assertEquals(sendNotificationNoPII.getCampaignId(), mappedSendNotificationDTO.getCampaignId());
    assertEquals(sendNotificationNoPII.getOrganizationId(), mappedSendNotificationDTO.getOrganizationId());
    assertEquals(sendNotificationNoPII.getIun(), mappedSendNotificationDTO.getIun());
    assertEquals(sendNotificationNoPII.getStatus(), mappedSendNotificationDTO.getStatus());
    assertEquals(sendNotificationNoPII.getSubject(), mappedSendNotificationDTO.getSubject());
    assertEquals(expectedPayments, mappedSendNotificationDTO.getPayments());
    assertEquals(sendNotificationNoPII.getHistory(), mappedSendNotificationDTO.getHistory());
  }

  private SendNotificationNoPII createSendNotificationNoPII(List<PuPayment> puPayments) {
    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO();
    recipient.setPuPayments(puPayments);

    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();

    sendNotificationNoPII.setSendNotificationId("12345");
    sendNotificationNoPII.setOrganizationId(1L);
    sendNotificationNoPII.setIun("IUN");
    sendNotificationNoPII.setStatus(NotificationStatus.IN_VALIDATION);
    sendNotificationNoPII.setRecipients(List.of(recipient));
    sendNotificationNoPII.setCampaignId("CAMPAIGN_ID1");
    sendNotificationNoPII.setSubject("SUBJECT");
    sendNotificationNoPII.setHistory(Collections.emptyList());

    return sendNotificationNoPII;
  }
}
