package it.gov.pagopa.pu.send.mapper;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.Address;
import it.gov.pagopa.pu.send.dto.generated.Attachment;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationPIIMapperTest {

  private PersonalDataService personalDataService;
  private DataCipherService dataCipherService;
  private SendNotificationPIIMapper sendNotificationPIIMapper;

  @BeforeEach
  void setUp() {
    personalDataService = Mockito.mock(PersonalDataService.class);
    sendNotificationPIIMapper = new SendNotificationPIIMapper(personalDataService, dataCipherService);
  }

  @Test
  void givenNoPIIWhenMapThenVerify() {
    // Given
    long personalDataId = 1L;
    SendNotificationNoPII noPii = getNoPII(personalDataId);

    SendNotificationPIIDTO piiDto = new SendNotificationPIIDTO();
    piiDto.setFiscalCode("RSSMRA80L05F593A");
    Address address = new Address();
    address.setAddress("Via Larga 10");
    address.setZip("00186");
    address.setMunicipality("Roma");
    address.setProvince("RM");
    piiDto.setAddress(address);

    Mockito.when(personalDataService.get(personalDataId, SendNotificationPIIDTO.class)).thenReturn(piiDto);

    // When
    SendNotification result = sendNotificationPIIMapper.map(noPii);

    // Then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals(noPii.getSendNotificationId(), result.getSendNotificationId());
    assertEquals(noPii.getOrganizationId(), result.getOrganizationId());
    assertEquals(noPii.getPaProtocolNumber(), result.getPaProtocolNumber());
    assertEquals(noPii.getSubjectType(), result.getSubjectType());
    assertEquals(piiDto.getFiscalCode(), result.getFiscalCode());
    assertEquals(piiDto.getAddress(), result.getAddress());
    assertEquals(noPii.getDenomination(), result.getDenomination());
    assertEquals(noPii.getPayments(), result.getPayments());
    assertEquals(noPii.getDocuments(), result.getDocuments());
    assertEquals(noPii.getStatus(), result.getStatus());
    assertEquals(noPii.getNotificationRequestId(), result.getNotificationRequestId());
    assertEquals(noPii.getIun(), result.getIun());
    assertEquals(noPii.getNotificationFeePolicy(), result.getNotificationFeePolicy());
    assertEquals(noPii.getPhysicalCommunicationType(), result.getPhysicalCommunicationType());
    assertEquals(noPii.getSenderDenomination(), result.getSenderDenomination());
    assertEquals(noPii.getSenderTaxId(), result.getSenderTaxId());
    assertEquals(noPii.getAmount(), result.getAmount());
    assertEquals(noPii.getPaymentExpirationDate(), result.getPaymentExpirationDate());
    assertEquals(noPii.getTaxonomyCode(), result.getTaxonomyCode());
    assertEquals(noPii.getPaFee(), result.getPaFee());
    assertEquals(noPii.getVat(), result.getVat());
    assertEquals(noPii.getPagoPaIntMode(), result.getPagoPaIntMode());
    assertEquals(noPii.getNotificationData(), result.getNotificationData());
    assertSame(noPii, result.getNoPII());
  }

  private static SendNotificationNoPII getNoPII(Long personalDataId) {
    SendNotificationNoPII noPii = new SendNotificationNoPII();
    noPii.setSendNotificationId("SNID001");
    noPii.setOrganizationId(2L);
    noPii.setPaProtocolNumber("PP001");
    noPii.setSubjectType("Individual");
    noPii.setPersonalDataId(personalDataId);
    noPii.setDenomination("Test Denomination");

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

    PuPayment puPayment = new PuPayment();
    puPayment.setDebtPositionId(0L);
    puPayment.setPayment(payment);


    noPii.setPayments(Collections.singletonList(puPayment));

    DocumentDTO documentAttachment = new DocumentDTO();
    documentAttachment.setFileName(attachment.getFileName());
    documentAttachment.setDigest(attachment.getDigest());
    documentAttachment.setContentType(attachment.getContentType());
    documentAttachment.setKey("docKey");
    documentAttachment.setVersionId("12345678");
    noPii.setDocuments(Collections.singletonList(documentAttachment));

    noPii.setStatus(NotificationStatus.WAITING_FILE);

    noPii.setNotificationRequestId("REQ001");
    noPii.setIun("IUN123");
    noPii.setNotificationFeePolicy("Policy001");
    noPii.setPhysicalCommunicationType("Digital");
    noPii.setSenderDenomination("Sender Org");
    noPii.setSenderTaxId("TAX001");
    noPii.setAmount(100);
    noPii.setPaymentExpirationDate("2026-01-01");
    noPii.setTaxonomyCode("CODE001");
    noPii.setPaFee(0);
    noPii.setVat(22);
    noPii.setPagoPaIntMode("PA");
    noPii.setNotificationData(OffsetDateTime.now());
    return noPii;
  }
}
