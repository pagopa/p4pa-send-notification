package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.send.connector.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.NotificationFeePolicyEnum;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.dto.generated.Recipient.RecipientTypeEnum;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.UnknownDebtPositionException;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class CreateNotificationRequest2SendNotificationPIIMapperTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;

  private CreateNotificationRequest2SendNotificationMapper mapper;

  @BeforeEach
  void init(){
    mapper = new CreateNotificationRequest2SendNotificationMapper(debtPositionServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionServiceMock);
  }

//  @Test
//  void givenCreateNotificationRequestWhenMapToModelThenOk(){
//    // Given
//    CreateNotificationRequest request = buildRequest();
//    String nav = request.getRecipients().getFirst().getPayments().getFirst().getPagoPa().getNoticeCode();
//
//    String accessToken = "ACCESSTOKEN";
//
//    DebtPosition debtPosition = new DebtPosition();
//    debtPosition.setDebtPositionId(3L);
//    Mockito.when(debtPositionServiceMock.findDebtPositionByInstallment(request.getOrganizationId(), nav, accessToken))
//      .thenReturn(debtPosition);
//
//    // When
//    SendNotification result = mapper.mapToModel(request, accessToken);
//
//    // Then
//    TestUtils.checkNotNullFields(result, "sendNotificationId","organizationId","notificationRequestId","iun","notificationDate", "personalDataId", "noPII");
//
//    Assertions.assertNotNull(result);
//    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
//    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
//    checkPayments(debtPosition, result);
//    checkDocuments(result);
//    Assertions.assertEquals(NotificationFeePolicyEnum.DELIVERY_MODE, NotificationFeePolicyEnum.valueOf(result.getNotificationFeePolicy()));
//    Assertions.assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, PhysicalCommunicationTypeEnum.valueOf(result.getPhysicalCommunicationType()));
//    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
//    Assertions.assertEquals("TAXID", result.getSenderTaxId());
//    Assertions.assertEquals(99999999, result.getAmount());
//    Assertions.assertEquals("TAXONOMYCODE", result.getTaxonomyCode());
//    Assertions.assertEquals(100, result.getPaFee());
//    Assertions.assertEquals(22, result.getVat());
//    Assertions.assertEquals(LocalDate.now().toString(), result.getPaymentExpirationDate());
//    Assertions.assertEquals(PagoPaIntModeEnum.NONE, PagoPaIntModeEnum.valueOf(result.getPagoPaIntMode()));
//  }

//  private static CreateNotificationRequest buildRequest() {
//    Recipient recipient = new Recipient();
//    recipient.setRecipientType(RecipientTypeEnum.PF);
//    recipient.setTaxId("RSSMRA80L05F593A");
//    recipient.setDenomination("ROSSI MARIO");
//
//    Payment payment = new Payment();
//    PagoPa pagoPa = new PagoPa();
//    pagoPa.setCreditorTaxId("CREDITORTAXID");
//    pagoPa.setNoticeCode("NOTICECODE");
//    pagoPa.setApplyCost(true);
//    Attachment attachment = new Attachment();
//    attachment.setFileName("attachment.pdf");
//    attachment.setDigest("sha256");
//    attachment.setContentType("application/pdf");
//    pagoPa.setAttachment(attachment);
//    payment.setPagoPa(pagoPa);
//    recipient.setPayments(Collections.singletonList(payment));
//
//    Address address = new Address();
//    address.setAddress("Via Larga 10");
//    address.setZip("00186");
//    address.setMunicipality("Roma");
//    address.setProvince("RM");
//    recipient.setPhysicalAddress(address);
//
//    Document document = new Document();
//    document.setFileName("document.pdf");
//    document.setContentType("application/pdf");
//    document.setDigest("sha256");
//
//    CreateNotificationRequest request = new CreateNotificationRequest();
//    request.setOrganizationId(1L);
//    request.setPaProtocolNumber("Prot_001");
//    request.setRecipient(recipient);
//    request.setDocuments(Collections.singletonList(document));
//    request.setNotificationFeePolicy(NotificationFeePolicyEnum.DELIVERY_MODE);
//    request.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER);
//    request.setSenderDenomination("SENDERDENOMINATION");
//    request.setSenderTaxId("TAXID");
//    request.setAmount(BigDecimal.valueOf(99999999));
//    request.setTaxonomyCode("TAXONOMYCODE");
//    request.setPaFee(100);
//    request.setVat(22);
//    request.setPaymentExpirationDate(LocalDate.now());
//    request.setPagoPaIntMode(PagoPaIntModeEnum.NONE);
//    return request;
//  }
//
//  private void checkPayments(DebtPosition debtPosition, SendNotification result) {
//    Assertions.assertSame(debtPosition.getDebtPositionId(), result.getPayments().getFirst().getDebtPositionId());
//    Assertions.assertEquals("CREDITORTAXID", result.getPayments().getFirst().getPayment().getPagoPa().getCreditorTaxId());
//    Assertions.assertEquals("NOTICECODE", result.getPayments().getFirst().getPayment().getPagoPa().getNoticeCode());
//    Assertions.assertEquals(true, result.getPayments().getFirst().getPayment().getPagoPa().getApplyCost());
//  }
//
//  private void checkDocuments(SendNotification result) {
//    Assertions.assertEquals("attachment.pdf", result.getDocuments().getFirst().getFileName());
//    Assertions.assertEquals("application/pdf", result.getDocuments().getFirst().getContentType());
//    Assertions.assertEquals("sha256", result.getDocuments().getFirst().getDigest());
//    Assertions.assertEquals("document.pdf", result.getDocuments().getLast().getFileName());
//    Assertions.assertEquals("application/pdf", result.getDocuments().getLast().getContentType());
//    Assertions.assertEquals("sha256", result.getDocuments().getLast().getDigest());
//    Assertions.assertEquals(NotificationStatus.WAITING_FILE, result.getStatus());
//    Assertions.assertEquals(FileStatus.WAITING, result.getDocuments().getFirst().getStatus());
//  }
//
//  @Test
//  void givenNoDebtPositionWhenThenThrow(){
//    String accessToken = "ACCESSTOKEN";
//    CreateNotificationRequest request = buildRequest();
//    String nav = request.getRecipient().getPayments().getFirst().getPagoPa().getNoticeCode();
//
//    Mockito.when(debtPositionServiceMock.findDebtPositionByInstallment(request.getOrganizationId(), nav, accessToken))
//      .thenReturn(null);
//
//    // When, Then
//    Assertions.assertThrows(UnknownDebtPositionException.class, () -> mapper.mapToModel(request, accessToken));
//  }
}
