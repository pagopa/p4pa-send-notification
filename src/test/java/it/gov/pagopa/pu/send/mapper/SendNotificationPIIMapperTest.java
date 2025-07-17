package it.gov.pagopa.pu.send.mapper;

import static it.gov.pagopa.pu.send.util.faker.DocumentFaker.buildDocumentDTO;
import static it.gov.pagopa.pu.send.util.faker.PuPaymentFaker.buildPuPayment;
import static it.gov.pagopa.pu.send.util.faker.PuRecipientFaker.buildPuRecipient;
import static it.gov.pagopa.pu.send.util.faker.SendNotificationFaker.buildSendNotification;
import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.send.dto.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationPIIMapperTest {

  @Mock
  private PersonalDataService personalDataService;
  @Mock
  private DataCipherService dataCipherService;
  private SendNotificationPIIMapper sendNotificationPIIMapper;

  @BeforeEach
  void setUp() {
    sendNotificationPIIMapper = new SendNotificationPIIMapper(personalDataService, dataCipherService);
  }

  @Test
  void givenNoPIIWhenMapThenVerify() {
    // Given
    long personalDataId = 1L;
    SendNotificationNoPII noPii = getNoPII(personalDataId);

    SendNotificationPIIDTO piiDto = new SendNotificationPIIDTO();
    List<PuRecipient> puRecipients = List.of(buildPuRecipient());
    piiDto.setPuRecipients(puRecipients);

    Mockito.when(personalDataService.get(personalDataId, SendNotificationPIIDTO.class)).thenReturn(piiDto);

    // When
    SendNotification result = sendNotificationPIIMapper.map(noPii);

    // Then
    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    assertEquals(noPii.getSendNotificationId(), result.getSendNotificationId());
    assertEquals(noPii.getOrganizationId(), result.getOrganizationId());
    assertEquals(noPii.getPaProtocolNumber(), result.getPaProtocolNumber());
    assertEquals(piiDto.getPuRecipients(), result.getPuRecipients());
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
    assertSame(noPii, result.getNoPII());
  }

  @Test
  void givenFullDTOWhenExtractNoPiiEntityThenVerify() {
    SendNotification sendNotification = getFullDTO();
    byte[] expectedHash = "BNRMHL75C06G702B".getBytes();

    Mockito.when(dataCipherService.hash(Mockito.anyString()))
      .thenReturn(expectedHash);

    SendNotificationNoPII result = sendNotificationPIIMapper.extractNoPiiEntity(sendNotification);

    TestUtils.checkNotNullFields(result, "personalDataId");
    assertNotNull(result);
    assertEquals(sendNotification.getSendNotificationId(), result.getSendNotificationId());
    assertEquals(expectedHash, result.getRecipients().getFirst().getFiscalCodeHash());
    assertEquals(sendNotification.getPuRecipients().getFirst().getPuPayments(), result.getRecipients().getFirst().getPuPayments());
  }


  @Test
  void givenFullDTOWhenExtractPiiEntityThenVerify() {
    SendNotification sendNotification = getFullDTO();

    SendNotificationPIIDTO result = sendNotificationPIIMapper.extractPiiDto(sendNotification);

    TestUtils.checkNotNullFields(result);
    assertNotNull(result);
    Assertions.assertEquals(sendNotification.getPuRecipients(), result.getPuRecipients());
  }
  private static SendNotificationNoPII getNoPII(Long personalDataId) {
    SendNotificationNoPII noPii = new SendNotificationNoPII();
    noPii.setSendNotificationId("SNID001");
    noPii.setOrganizationId(2L);
    noPii.setPaProtocolNumber("PP001");
    noPii.setPersonalDataId(personalDataId);

    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO("HASHED_TAX_ID".getBytes(), List.of(buildPuPayment()));
    noPii.setRecipients(List.of(recipient));

    noPii.setDocuments(List.of(buildDocumentDTO()));
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
    return noPii;
  }

  private static SendNotification getFullDTO() {
    SendNotification sendNotification = buildSendNotification();
    sendNotification.setNoPII(getNoPII(1L));
    return sendNotification;
  }

}
