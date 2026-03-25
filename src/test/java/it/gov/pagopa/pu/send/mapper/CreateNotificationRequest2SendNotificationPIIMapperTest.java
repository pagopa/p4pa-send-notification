package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.send.connector.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.send.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.NotificationFeePolicyEnum;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.dto.generated.Document;
import it.gov.pagopa.pu.send.dto.generated.Recipient;
import it.gov.pagopa.pu.send.dto.generated.Recipient.RecipientTypeEnum;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.UnknownDebtPositionException;
import it.gov.pagopa.pu.send.util.DebtPositionUtils;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static it.gov.pagopa.pu.send.util.faker.DocumentFaker.buildDocument;
import static it.gov.pagopa.pu.send.util.faker.RecipientFaker.buildRecipient;

@ExtendWith(MockitoExtension.class)
class CreateNotificationRequest2SendNotificationPIIMapperTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private CreateNotificationRequest2SendNotificationMapper mapper;

  @BeforeEach
  void init() {
    mapper = new CreateNotificationRequest2SendNotificationMapper(
      debtPositionServiceMock,
      brokerServiceMock,
      organizationServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionServiceMock,
      brokerServiceMock,
      organizationServiceMock
    );
  }

  @Test
  void givenCreateNotificationRequestWhenMapToModelThenOk() {
    // Given
    CreateNotificationRequest request = buildRequest();
    String accessToken = "ACCESSTOKEN";

    Broker broker = Mockito.mock(Broker.class);

    Mockito.when(broker.getPagoPaInteractionModel())
      .thenReturn(PagoPaInteractionModel.SYNC);

    Mockito.when(brokerServiceMock.getBrokerByOrganizationId(request.getOrganizationId(), accessToken))
      .thenReturn(broker);

    String nav = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getNoticeCode();
    String orgFiscalCode = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getCreditorTaxId();
    String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

    DebtPositionDTO debtPosition = new DebtPositionDTO();
    debtPosition.setDebtPositionId(3L);

    Mockito.when(debtPositionServiceMock.findDebtPositionByInstallment(request.getOrganizationId(), nav, accessToken))
      .thenReturn(debtPosition);

    Mockito.when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.of(new Organization()));

    // When
    SendNotification result = mapper.mapToModel(request, accessToken);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId", "organizationId", "notificationRequestId", "iun", "notificationDate", "personalDataId", "noPII", "legalFacts");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
    checkPayments(debtPosition, result);
    checkDocuments(result);
    Assertions.assertEquals(NotificationFeePolicyEnum.DELIVERY_MODE.getValue(), result.getNotificationFeePolicy());
    Assertions.assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER.getValue(), result.getPhysicalCommunicationType());
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("TAXID", result.getSenderTaxId());
    Assertions.assertEquals(99999999, result.getAmount());
    Assertions.assertEquals("TAXONOMYCODE", result.getTaxonomyCode());
    Assertions.assertEquals(100, result.getPaFee());
    Assertions.assertEquals(22, result.getVat());
    Assertions.assertEquals(LocalDate.now().toString(), result.getPaymentExpirationDate());
    Assertions.assertEquals("SYNC", result.getPagoPaIntMode());
  }

  @Test
  void givenCreateNotificationRequestWithPaymentOutsidePUWhenMapToModelThenOk() {
    // Given
    CreateNotificationRequest request = buildRequest();
    String accessToken = "ACCESSTOKEN";

    Broker broker = Mockito.mock(Broker.class);

    Mockito.when(broker.getPagoPaInteractionModel())
      .thenReturn(PagoPaInteractionModel.SYNC);

    Mockito.when(brokerServiceMock.getBrokerByOrganizationId(request.getOrganizationId(), accessToken))
      .thenReturn(broker);

    String nav = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getNoticeCode();
    String orgFiscalCode = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getCreditorTaxId();
    String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

    Mockito.when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.empty());

    // When
    SendNotification result = mapper.mapToModel(request, accessToken);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId", "organizationId", "notificationRequestId", "iun", "notificationDate", "personalDataId", "noPII", "legalFacts");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
    Assertions.assertNull(result.getPuRecipients().getFirst().getPuPayments().getFirst());
    checkDocuments(result);
    Assertions.assertEquals(NotificationFeePolicyEnum.DELIVERY_MODE.getValue(), result.getNotificationFeePolicy());
    Assertions.assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER.getValue(), result.getPhysicalCommunicationType());
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("TAXID", result.getSenderTaxId());
    Assertions.assertEquals(99999999, result.getAmount());
    Assertions.assertEquals("TAXONOMYCODE", result.getTaxonomyCode());
    Assertions.assertEquals(100, result.getPaFee());
    Assertions.assertEquals(22, result.getVat());
    Assertions.assertEquals(LocalDate.now().toString(), result.getPaymentExpirationDate());
    Assertions.assertEquals("SYNC", result.getPagoPaIntMode());

    Mockito.verifyNoInteractions(debtPositionServiceMock);
  }

  @ParameterizedTest
  @ValueSource(strings = {"pagoPaNull", "f24null", "bothNull"})
  void givenCreateNotificationRequestWithSomeNullValuesWhenMapToModelThenOk(String paymentNull) {
    // Given
    CreateNotificationRequest request = buildRequest();
    request.setDocuments(new ArrayList<>());
    request.setAmount(null);
    request.setPaFee(null);
    request.setPaymentExpirationDate(null);
    request.setVat(null);
    request.getRecipients().getFirst().getPayments().getFirst().getPagoPa().setAttachment(null);
    if (paymentNull.equals("pagoPaNull")) {
      request.getRecipients().getFirst().getPayments().getFirst().setPagoPa(null);
    } else if (paymentNull.equals("f24null")) {
      request.getRecipients().getFirst().getPayments().getFirst().setF24(null);
    } else {
      request.getRecipients().getFirst().getPayments().getFirst().setPagoPa(null);
      request.getRecipients().getFirst().getPayments().getFirst().setF24(null);
    }

    String accessToken = "ACCESSTOKEN";

    Broker broker = Mockito.mock(Broker.class);

    Mockito.when(broker.getPagoPaInteractionModel())
      .thenReturn(PagoPaInteractionModel.ASYNC_GPD);

    Mockito.when(brokerServiceMock.getBrokerByOrganizationId(request.getOrganizationId(), accessToken))
      .thenReturn(broker);


    DebtPositionDTO debtPosition = new DebtPositionDTO();
    debtPosition.setDebtPositionId(3L);

    if (paymentNull.equals("f24null")) {
      String nav = request.getRecipients().getFirst().getPayments().getFirst().getPagoPa().getNoticeCode();
      String orgFiscalCode = request.getRecipients().getFirst()
        .getPayments().getFirst().getPagoPa().getCreditorTaxId();
      String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

      Mockito.when(debtPositionServiceMock.findDebtPositionByInstallment(request.getOrganizationId(), nav, accessToken))
        .thenReturn(debtPosition);
      Mockito.when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
        .thenReturn(Optional.of(new Organization()));
    }

    // When
    SendNotification result = mapper.mapToModel(request, accessToken);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId", "organizationId", "notificationRequestId", "iun",
      "notificationDate", "personalDataId", "noPII", "paymentExpirationDate", "legalFacts");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
    if (paymentNull.equals("pagoPaNull")) {
      checkPayments(null, result);
    } else if (paymentNull.equals("f24null")) {
      checkPayments(debtPosition, result);
    }
    Assertions.assertEquals(NotificationFeePolicyEnum.DELIVERY_MODE.getValue(), result.getNotificationFeePolicy());
    Assertions.assertEquals(PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER.getValue(), result.getPhysicalCommunicationType());
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("TAXID", result.getSenderTaxId());
    Assertions.assertEquals("TAXONOMYCODE", result.getTaxonomyCode());
    Assertions.assertEquals("ASYNC", result.getPagoPaIntMode());
  }

  @Test
  void givenNoDebtPositionWhenThenThrow() {
    String accessToken = "ACCESSTOKEN";
    CreateNotificationRequest request = buildRequest();
    String nav = request.getRecipients().getFirst().getPayments().getFirst().getPagoPa().getNoticeCode();
    String orgFiscalCode = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getCreditorTaxId();
    String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

    Mockito.when(debtPositionServiceMock.findDebtPositionByInstallment(request.getOrganizationId(), nav, accessToken))
      .thenReturn(null);
    Mockito.when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.of(new Organization()));

    Assertions.assertThrows(UnknownDebtPositionException.class, () -> mapper.mapToModel(request, accessToken));
  }

  private static CreateNotificationRequest buildRequest() {
    Recipient recipient = buildRecipient();
    Document document = buildDocument();

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setOrganizationId(1L);
    request.setPaProtocolNumber("Prot_001");
    request.setRecipients(Collections.singletonList(recipient));
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
    return request;
  }

  private void checkPayments(DebtPositionDTO debtPosition, SendNotification result) {
    if (debtPosition != null) {
      Assertions.assertSame(debtPosition.getDebtPositionId(), result.getPuRecipients().getFirst().getPuPayments().getFirst().getDebtPositionId());
      Assertions.assertEquals("CREDITORTAXID", result.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().getCreditorTaxId());
      Assertions.assertEquals("NOTICECODE", result.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().getNoticeCode());
      Assertions.assertEquals(true, result.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().getApplyCost());
    }
  }

  private void checkDocuments(SendNotification result) {
    Assertions.assertEquals("attachment", result.getDocuments().getFirst().getFileName());
    Assertions.assertEquals("application/pdf", result.getDocuments().getFirst().getContentType());
    Assertions.assertEquals("sha256", result.getDocuments().getFirst().getDigest());
    Assertions.assertEquals("document.pdf", result.getDocuments().getLast().getFileName());
    Assertions.assertEquals("application/pdf", result.getDocuments().getLast().getContentType());
    Assertions.assertEquals("sha256", result.getDocuments().getLast().getDigest());
    Assertions.assertEquals(NotificationStatus.WAITING_FILE, result.getStatus());
    Assertions.assertEquals(FileStatus.WAITING, result.getDocuments().getFirst().getStatus());
  }

}
