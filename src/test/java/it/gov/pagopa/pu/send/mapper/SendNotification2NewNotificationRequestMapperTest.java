package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.Address;
import it.gov.pagopa.pu.send.dto.generated.Attachment;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SendNotification2NewNotificationRequestMapperTest {

  @Mock
  private SendNotificationMapper sendNotificationMapperMock;

  @InjectMocks
  private SendNotification2NewNotificationRequestMapper mapper;


  @Test
  void givenSendNotificationWhenMapThenOk() {
    // Given
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("12345");
    sendNotification.setPaProtocolNumber("Prot_001");
    sendNotification.setSubjectType("PF");
    sendNotification.setFiscalCode("BNRMHL75C06G702B");
    sendNotification.setDenomination("Michelangelo Buonarroti");

    Address address = new Address();
    address.setAddress("Via Larga 10");
    address.setZip("00186");
    address.setMunicipality("Roma");
    address.setProvince("RM");
    sendNotification.setAddress(address);

    //Payments
    Payment payment = new Payment();
    PagoPa pagoPa = new PagoPa();
    pagoPa.setCreditorTaxId("CREDITORTAXID");
    pagoPa.setNoticeCode("NOTICECODE");
    pagoPa.setApplyCost(true);
    Attachment attachment = new Attachment();
    attachment.setContentType("application/pdf");
    attachment.setDigest("sha256");
    attachment.setFileName("attachment");
    pagoPa.setAttachment(attachment);
    payment.setPagoPa(pagoPa);

    DocumentDTO documentAttachment = new DocumentDTO();
    documentAttachment.setFileName(attachment.getFileName());
    documentAttachment.setDigest(attachment.getDigest());
    documentAttachment.setContentType(attachment.getContentType());
    documentAttachment.setKey("docKey");
    documentAttachment.setVersionId("12345678");

    sendNotification.setPayments(Collections.singletonList(new PuPayment(1L, payment)));
    // end payments


    DocumentDTO document = new DocumentDTO();
    document.setFileName("document");
    document.setDigest("sha256");
    document.setContentType("application/pdf");
    document.setKey("docKey");
    document.setVersionId("12345678");

    List<DocumentDTO> documents = new ArrayList<>();
    documents.add(documentAttachment);
    documents.add(document);
    sendNotification.setDocuments(documents);

    sendNotification.setNotificationFeePolicy(NotificationFeePolicyDTO.DELIVERY_MODE.getValue());
    sendNotification.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER.getValue());
    sendNotification.setSenderDenomination("Ente Intermediario 2");
    sendNotification.setSenderTaxId("00000000018");
    sendNotification.setAmount(100);
    sendNotification.setTaxonomyCode("010101P");
    sendNotification.setPaFee(100);
    sendNotification.setVat(22);
    sendNotification.setPaymentExpirationDate("2025-12-31");
    sendNotification.setPagoPaIntMode(PagoPaIntModeEnum.NONE.getValue());

    SendNotificationNoPII noPII = new SendNotificationNoPII();

    Mockito.when(sendNotificationMapperMock.map(Mockito.any(SendNotificationNoPII.class))).thenReturn(sendNotification);

    // When
    NewNotificationRequestV24DTO result = mapper.apply(noPII);

    // Then
    TestUtils.checkNotNullFields(result,"_abstract","cancelledIun","group","amount","paymentExpirationDate","pagoPaIntMode");

    assertNotNull(result);
    assertEquals("12345", result.getIdempotenceToken());
    assertEquals("Prot_001", result.getPaProtocolNumber());
    assertEquals("TEST notifica PU numero 12345", result.getSubject());

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

  private static void checkRecipient(NewNotificationRequestV24DTO result) {
    NotificationRecipientV23DTO recipient = result.getRecipients().getFirst();
    assertEquals(RecipientTypeEnum.PF, recipient.getRecipientType());
    assertEquals("BNRMHL75C06G702B", recipient.getTaxId());
    assertEquals("Michelangelo Buonarroti", recipient.getDenomination());

    NotificationPhysicalAddressDTO address = recipient.getPhysicalAddress();
    assertEquals("Via Larga 10", address.getAddress());
    assertEquals("00186", address.getZip());
    assertEquals("Roma", address.getMunicipality());
    assertEquals("RM", address.getProvince());
  }

  private static void checkDocuments(NewNotificationRequestV24DTO result) {
    NotificationDocumentDTO resultDocument = result.getDocuments().getFirst();
    assertEquals("sha256", resultDocument.getDigests().getSha256());
    assertEquals("application/pdf", resultDocument.getContentType());
    assertEquals("docKey", resultDocument.getRef().getKey());
    assertEquals("12345678", resultDocument.getRef().getVersionToken());
  }
}
