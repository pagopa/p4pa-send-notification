package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.Attachment;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.NotificationFeePolicyEnum;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.dto.generated.Document;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.Recipient;
import it.gov.pagopa.pu.send.dto.generated.Recipient.RecipientTypeEnum;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.util.TestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateNotificationRequest2SendNotificationMapperTest {

  private final CreateNotificationRequest2SendNotificationMapper mapper = new CreateNotificationRequest2SendNotificationMapper();

  @Test
  void givenCreateNotificationRequestWhenMapThenOk(){
    // Given
    Recipient recipient = new Recipient();
    recipient.setRecipientType(RecipientTypeEnum.PF);
    recipient.setTaxId("RSSMRA80L05F593A");
    recipient.setDenomination("ROSSI MARIO");

    Payment payment = new Payment();
    PagoPa pagoPa = new PagoPa();
    pagoPa.setCreditorTaxId("CREDITORTAXID");
    pagoPa.setNoticeCode("NOTICECODE");
    pagoPa.setApplyCost(true);
    Attachment attachment = new Attachment();
    attachment.setFileName("attachment.pdf");
    attachment.setDigest("sha256");
    attachment.setContentType("application/pdf");
    pagoPa.setAttachment(attachment);
    payment.setPagoPa(pagoPa);
    recipient.setPayments(Collections.singletonList(payment));

    Document document = new Document();
    document.setFileName("document.pdf");
    document.setContentType("application/pdf");
    document.setDigest("sha256");

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setPaProtocolNumber("Prot_001");
    request.setRecipient(recipient);
    request.setDocuments(Collections.singletonList(document));
    request.setNotificationFeePolicy(NotificationFeePolicyEnum.DELIVERY_MODE);
    request.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER);
    request.setSenderDenomination("SENDERDENOMINATION");
    request.setSenderTaxId("TAXID");
    request.setAmount(BigDecimal.valueOf(99999999));
    request.setTaxonomyCode("TAXONOMYCODE");
    request.setPaFee(100);
    request.setVat(22);
    request.setPaymentExpirationDate(LocalDate.now());
    request.setPagoPaIntMode(PagoPaIntModeEnum.NONE);

    Long organizationId = 1L;

    // When
    SendNotification result = mapper.map(request, organizationId);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId","organizationId","notificationRequestId","iun","notificationData");

    Assertions.assertNotNull(result);
    Assertions.assertEquals("PF", result.getSubjectType());
    Assertions.assertEquals("RSSMRA80L05F593A", result.getFiscalCode());
    Assertions.assertEquals("ROSSI MARIO", result.getDenomination());
    Assertions.assertEquals("CREDITORTAXID", result.getPayments().getFirst().getPagoPa().getCreditorTaxId());
    Assertions.assertEquals("NOTICECODE", result.getPayments().getFirst().getPagoPa().getNoticeCode());
    Assertions.assertEquals(true, result.getPayments().getFirst().getPagoPa().getApplyCost());
    Assertions.assertEquals("attachment.pdf", result.getDocuments().getFirst().getFileName());
    Assertions.assertEquals("application/pdf", result.getDocuments().getFirst().getContentType());
    Assertions.assertEquals("sha256", result.getDocuments().getFirst().getDigest());
    Assertions.assertEquals("document.pdf", result.getDocuments().getLast().getFileName());
    Assertions.assertEquals("application/pdf", result.getDocuments().getLast().getContentType());
    Assertions.assertEquals("sha256", result.getDocuments().getLast().getDigest());
    Assertions.assertEquals(NotificationStatus.WAITING_FILE, result.getStatus());
    Assertions.assertEquals(FileStatus.WAITING, result.getDocuments().getFirst().getStatus());
    Assertions.assertEquals(NotificationFeePolicyEnum.DELIVERY_MODE, NotificationFeePolicyEnum.valueOf(result.getNotificationFeePolicy()));
    Assertions.assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, PhysicalCommunicationTypeEnum.valueOf(result.getPhysicalCommunicationType()));
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("TAXID", result.getSenderTaxId());
    Assertions.assertEquals(99999999, result.getAmount());
    Assertions.assertEquals("TAXONOMYCODE", result.getTaxonomyCode());
    Assertions.assertEquals(100, result.getPaFee());
    Assertions.assertEquals(22, result.getVat());
    Assertions.assertEquals(LocalDate.now().toString(), result.getPaymentExpirationDate());
    Assertions.assertEquals(PagoPaIntModeEnum.NONE, PagoPaIntModeEnum.valueOf(result.getPagoPaIntMode()));
  }
}
