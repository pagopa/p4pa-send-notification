package it.gov.pagopa.pu.send.mapper;

import static org.junit.jupiter.api.Assertions.*;


import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationDocumentDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationFeePolicyDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPhysicalAddressDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.util.TestUtils;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotification2NewNotificationRequestMapperTest {

  private final SendNotification2NewNotificationRequestMapper mapper = new SendNotification2NewNotificationRequestMapper();
  @Test
  void givenSendNotificationWhenMapThenOk() {
    // Given
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("12345");
    sendNotification.setPaProtocolNumber("Prot_001");
    sendNotification.setSubjectType("PF");
    sendNotification.setFiscalCode("BNRMHL75C06G702B");

    DocumentDTO document = new DocumentDTO();
    document.setDigest("sha256");
    document.setContentType("application/pdf");
    document.setKey("docKey");
    document.setVersionId("12345678");

    sendNotification.setDocuments(Collections.singletonList(document));

    // When
    NewNotificationRequestV24DTO result = mapper.apply(sendNotification);

    // Then
    TestUtils.checkNotNullFields(result,"_abstract","cancelledIun","group","amount","paymentExpirationDate","paFee","vat","pagoPaIntMode");

    assertNotNull(result);
    assertEquals("12345", result.getIdempotenceToken());
    assertEquals("Prot_001", result.getPaProtocolNumber());
    assertEquals("TEST notifica PU numero 12345", result.getSubject());

    NotificationRecipientV23DTO recipient = result.getRecipients().getFirst();
    assertEquals(RecipientTypeEnum.PF, recipient.getRecipientType());
    assertEquals("BNRMHL75C06G702B", recipient.getTaxId());
    assertEquals("Michelangelo Buonarroti", recipient.getDenomination());

    NotificationPhysicalAddressDTO address = recipient.getPhysicalAddress();
    assertEquals("Via Larga 10", address.getAddress());
    assertEquals("00186", address.getZip());
    assertEquals("Roma", address.getMunicipality());
    assertEquals("RM", address.getProvince());

    NotificationDocumentDTO resultDocument = result.getDocuments().getFirst();
    assertEquals("sha256", resultDocument.getDigests().getSha256());
    assertEquals("application/pdf", resultDocument.getContentType());
    assertEquals("docKey", resultDocument.getRef().getKey());
    assertEquals("12345678", resultDocument.getRef().getVersionToken());

    assertEquals(NotificationFeePolicyDTO.FLAT_RATE, result.getNotificationFeePolicy());
    assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    assertEquals("Ente Intermediario 2", result.getSenderDenomination());
    assertEquals("00000000018", result.getSenderTaxId());
    assertEquals("010101P", result.getTaxonomyCode());
  }
}
