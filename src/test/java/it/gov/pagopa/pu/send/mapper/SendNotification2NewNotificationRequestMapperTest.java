package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.send.util.faker.DocumentFaker.buildDocumentAttachment;
import static it.gov.pagopa.pu.send.util.faker.SendNotificationFaker.buildSendNotification;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SendNotification2NewNotificationRequestMapperTest {

  @Mock
  private SendNotificationPIIMapper sendNotificationPIIMapperMock;

  @InjectMocks
  private SendNotification2NewNotificationRequestMapper mapper;

  @Test
  void givenSendNotificationWhenMapThenOk() {
    //given
    SendNotification sendNotification = buildSendNotification();
    SendNotificationNoPII noPII = new SendNotificationNoPII();

    Mockito.when(sendNotificationPIIMapperMock.map(Mockito.any(SendNotificationNoPII.class))).thenReturn(sendNotification);

    //when
    NewNotificationRequestV24DTO result = mapper.apply(noPII);

    //then
    TestUtils.checkNotNullFields(result, "_abstract", "cancelledIun", "group", "amount", "paymentExpirationDate", "pagoPaIntMode");

    assertNotNull(result);
    assertEquals("12345", result.getIdempotenceToken());
    assertEquals("Prot_001", result.getPaProtocolNumber());
    assertEquals("Notifica Piattaforma Unitaria", result.getSubject());

    checkRecipient(result);
    checkDocuments(result);

    assertEquals(NotificationFeePolicyDTO.DELIVERY_MODE, result.getNotificationFeePolicy());
    assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    assertEquals("Ente Intermediario 2", result.getSenderDenomination());
    assertEquals("00000000018", result.getSenderTaxId());
    assertEquals("010101P", result.getTaxonomyCode());
    assertEquals(100, result.getAmount());
    assertEquals(100, result.getPaFee());
    assertEquals(22, result.getVat());
    assertEquals("2025-12-31", result.getPaymentExpirationDate());
    assertEquals(NewNotificationRequestV24DTO.PagoPaIntModeEnum.NONE, result.getPagoPaIntMode());
  }

  @Test
  void givenSendNotificationWithSomeNullValueWhenMapThenOk() {
    //given
    SendNotification sendNotification = buildSendNotification();
    sendNotification.setVat(0);
    sendNotification.setPaFee(0);
    sendNotification.setAmount(0);
    sendNotification.setPaymentExpirationDate(null);
    sendNotification.setPagoPaIntMode(null);
    sendNotification.getPuRecipients().getFirst().getRecipient().setDigitalDomicile(null);
    sendNotification.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().setAttachment(null);
    sendNotification.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().setF24(null);
    SendNotificationNoPII noPII = new SendNotificationNoPII();

    Mockito.when(sendNotificationPIIMapperMock.map(Mockito.any(SendNotificationNoPII.class))).thenReturn(sendNotification);

    //when
    NewNotificationRequestV24DTO result = mapper.apply(noPII);

    //then
    TestUtils.checkNotNullFields(result, "_abstract", "cancelledIun", "group", "amount", "paymentExpirationDate", "pagoPaIntMode", "paFee", "vat");

    assertNotNull(result);
    assertEquals("12345", result.getIdempotenceToken());
    assertEquals("Prot_001", result.getPaProtocolNumber());
    assertEquals("Notifica Piattaforma Unitaria", result.getSubject());

    checkRecipient(result);
    checkDocuments(result);

    assertEquals(NotificationFeePolicyDTO.DELIVERY_MODE, result.getNotificationFeePolicy());
    assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    assertEquals("Ente Intermediario 2", result.getSenderDenomination());
    assertEquals("00000000018", result.getSenderTaxId());
    assertEquals("010101P", result.getTaxonomyCode());
  }

  @Test
  void givenSendNotificationWithDocDiffersValueWhenMapThenOk() {
    //given
    SendNotification sendNotification = buildSendNotification();
    sendNotification.setVat(0);
    sendNotification.setPaFee(0);
    sendNotification.setAmount(0);
    sendNotification.setPaymentExpirationDate(null);
    sendNotification.setPagoPaIntMode(null);
    sendNotification.getPuRecipients().getFirst().getRecipient().setDigitalDomicile(null);
    sendNotification.setDocuments(List.of(buildDocumentAttachment()));
    SendNotificationNoPII noPII = new SendNotificationNoPII();

    Mockito.when(sendNotificationPIIMapperMock.map(Mockito.any(SendNotificationNoPII.class))).thenReturn(sendNotification);

    //when
    NewNotificationRequestV24DTO result = mapper.apply(noPII);

    //then
    TestUtils.checkNotNullFields(result, "_abstract", "cancelledIun", "group", "amount", "paymentExpirationDate", "pagoPaIntMode", "paFee", "vat");

    assertNotNull(result);
    assertEquals("12345", result.getIdempotenceToken());
    assertEquals("Prot_001", result.getPaProtocolNumber());
    assertEquals("Notifica Piattaforma Unitaria", result.getSubject());

    checkRecipient(result);
    checkDocuments(result);

    assertEquals(NotificationFeePolicyDTO.DELIVERY_MODE, result.getNotificationFeePolicy());
    assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    assertEquals("Ente Intermediario 2", result.getSenderDenomination());
    assertEquals("00000000018", result.getSenderTaxId());
    assertEquals("010101P", result.getTaxonomyCode());
  }

  private static void checkRecipient(NewNotificationRequestV24DTO result) {
    NotificationRecipientV23DTO recipient = result.getRecipients().getFirst();
    assertEquals("BNRMHL75C06G702B", recipient.getTaxId());
    assertEquals("ROSSI MARIO", recipient.getDenomination());

    NotificationPhysicalAddressDTO address = recipient.getPhysicalAddress();
    assertEquals("Via Larga 10", address.getAddress());
    assertEquals("00186", address.getZip());
    assertEquals("Roma", address.getMunicipality());
    assertEquals("RM", address.getProvince());

    NotificationDigitalAddressDTO digital = recipient.getDigitalDomicile();
    if (digital != null) {
      assertEquals("account@domain.it", digital.getAddress());
      assertEquals(NotificationDigitalAddressDTO.TypeEnum.PEC, digital.getType());
    }

    NotificationPaymentItemDTO payment = recipient.getPayments().getFirst();
    assertEquals("CREDITORTAXID", payment.getPagoPa().getCreditorTaxId());
    assertEquals("NOTICECODE", payment.getPagoPa().getNoticeCode());
    assertTrue(payment.getPagoPa().getApplyCost());

    if (payment.getPagoPa().getAttachment() != null) {
      assertNotNull(payment.getPagoPa().getAttachment());
      assertEquals("sha256", payment.getPagoPa().getAttachment().getDigests().getSha256());
      assertEquals("application/pdf", payment.getPagoPa().getAttachment().getContentType());
      assertEquals("docKey", payment.getPagoPa().getAttachment().getRef().getKey());
      assertEquals("12345678", payment.getPagoPa().getAttachment().getRef().getVersionToken());
    }
  }

  private static void checkDocuments(NewNotificationRequestV24DTO result) {
    NotificationDocumentDTO resultDocument = result.getDocuments().getFirst();
    assertEquals("sha256", resultDocument.getDigests().getSha256());
    assertEquals("application/pdf", resultDocument.getContentType());
    assertEquals("docKey", resultDocument.getRef().getKey());
    assertEquals("12345678", resultDocument.getRef().getVersionToken());
  }
}
