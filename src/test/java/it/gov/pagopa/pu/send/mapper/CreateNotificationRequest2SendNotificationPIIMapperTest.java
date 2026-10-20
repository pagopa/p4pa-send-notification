package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.send.connector.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.dto.generated.Recipient.RecipientTypeEnum;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.common.NotFoundException;
import it.gov.pagopa.pu.send.exception.UnknownDebtPositionException;
import it.gov.pagopa.pu.send.util.DebtPositionUtils;
import it.gov.pagopa.pu.send.util.TestUtils;
import it.gov.pagopa.send.dto.generated.NewNotificationRequestV25DTO;
import it.gov.pagopa.send.dto.generated.NotificationFeePolicyDTO;
import it.gov.pagopa.send.dto.generated.SentNotificationV25DTO;
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
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.send.util.faker.DocumentFaker.buildDocument;
import static it.gov.pagopa.pu.send.util.faker.RecipientFaker.buildRecipient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNotificationRequest2SendNotificationPIIMapperTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private CreateNotificationRequest2SendNotificationMapper mapper;

  @BeforeEach
  void init() {
    mapper = new CreateNotificationRequest2SendNotificationMapper(
      debtPositionServiceMock,
      organizationServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionServiceMock,
      organizationServiceMock
    );
  }

  @Test
  void givenCreateNotificationRequestWhenMapToModelThenOk() {
    // Given
    CreateNotificationRequest request = buildRequest();
    String accessToken = "ACCESSTOKEN";

    OrganizationStationDTO organizationStationDTO = new OrganizationStationDTO();
    organizationStationDTO.setPagoPaInteractionModel(PagoPaInteractionModel.SYNC);

    when(organizationServiceMock.findOrganizationStation(request.getOrganizationId(), null, accessToken))
      .thenReturn(Optional.of(organizationStationDTO));

    String nav = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getNoticeCode();
    String orgFiscalCode = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getCreditorTaxId();
    String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

    DebtPositionDTO debtPosition = new DebtPositionDTO();
    debtPosition.setDebtPositionId(3L);
    Organization organization = new Organization();
    organization.setOrganizationId(request.getOrganizationId());

    PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();

    InstallmentDTO installmentDTO = new InstallmentDTO();
    installmentDTO.setStatus(InstallmentStatus.PAID);
    installmentDTO.setNav(nav);
    paymentOptionDTO.setInstallments(List.of(installmentDTO));

    debtPosition.setPaymentOptions(List.of(paymentOptionDTO));

    when(debtPositionServiceMock.findDebtPositionByInstallment(organization.getOrganizationId(), nav, accessToken))
      .thenReturn(debtPosition);

    when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.of(organization));

    // When
    SendNotification result = mapper.mapToModel(request, null, accessToken);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId", "organizationId", "notificationRequestId", "iun", "notificationDate", "personalDataId", "noPII", "legalFacts", "campaignId", "taxonomyDescription");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
    checkPayments(debtPosition, result);
    checkDocuments(result);
    Assertions.assertEquals(NotificationFeePolicyDTO.DELIVERY_MODE, result.getNotificationFeePolicy());
    Assertions.assertEquals(SentNotificationV25DTO.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    Assertions.assertEquals("Test Notifica Piattaforma Unitaria", result.getSubject());
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("SENDERTAXID", result.getSenderTaxId());
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

    OrganizationStationDTO organizationStationDTO = new OrganizationStationDTO();
    organizationStationDTO.setPagoPaInteractionModel(PagoPaInteractionModel.SYNC);

    when(organizationServiceMock.findOrganizationStation(request.getOrganizationId(), null, accessToken))
      .thenReturn(Optional.of(organizationStationDTO));

    String nav = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getNoticeCode();
    String orgFiscalCode = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getCreditorTaxId();
    String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

    when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.empty());

    // When
    SendNotification result = mapper.mapToModel(request, null, accessToken);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId", "organizationId", "notificationRequestId", "iun", "notificationDate", "personalDataId", "noPII", "legalFacts", "campaignId", "taxonomyDescription");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
    checkDocuments(result);
    Assertions.assertEquals(NotificationFeePolicyDTO.DELIVERY_MODE, result.getNotificationFeePolicy());
    Assertions.assertEquals(SentNotificationV25DTO.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    Assertions.assertEquals("Test Notifica Piattaforma Unitaria", result.getSubject());
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("SENDERTAXID", result.getSenderTaxId());
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

    OrganizationStationDTO organizationStationDTO = new OrganizationStationDTO();
    organizationStationDTO.setPagoPaInteractionModel(PagoPaInteractionModel.ASYNC_GPD);

    when(organizationServiceMock.findOrganizationStation(request.getOrganizationId(), null, accessToken))
      .thenReturn(Optional.of(organizationStationDTO));


    DebtPositionDTO debtPosition = new DebtPositionDTO();
    debtPosition.setDebtPositionId(3L);
    Organization organization = new Organization();
    organization.setOrganizationId(request.getOrganizationId());

    if (paymentNull.equals("f24null")) {
      String nav = request.getRecipients().getFirst().getPayments().getFirst().getPagoPa().getNoticeCode();
      String orgFiscalCode = request.getRecipients().getFirst()
        .getPayments().getFirst().getPagoPa().getCreditorTaxId();
      String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

      PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();

      InstallmentDTO installmentDTO = new InstallmentDTO();
      installmentDTO.setStatus(InstallmentStatus.PAID);
      installmentDTO.setNav(nav);
      paymentOptionDTO.setInstallments(List.of(installmentDTO));

      debtPosition.setPaymentOptions(List.of(paymentOptionDTO));

      when(debtPositionServiceMock.findDebtPositionByInstallment(organization.getOrganizationId(), nav, accessToken))
        .thenReturn(debtPosition);
      when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
        .thenReturn(Optional.of(organization));
    }

    // When
    SendNotification result = mapper.mapToModel(request, null, accessToken);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId", "organizationId", "notificationRequestId", "iun",
      "notificationDate", "personalDataId", "noPII", "paymentExpirationDate", "legalFacts", "campaignId", "taxonomyDescription");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(RecipientTypeEnum.PF, result.getPuRecipients().getFirst().getRecipient().getRecipientType());
    Assertions.assertEquals("ROSSI MARIO", result.getPuRecipients().getFirst().getRecipient().getDenomination());
    if (paymentNull.equals("pagoPaNull")) {
      checkPayments(null, result);
    } else if (paymentNull.equals("f24null")) {
      checkPayments(debtPosition, result);
    }
    Assertions.assertEquals(NotificationFeePolicyDTO.DELIVERY_MODE, result.getNotificationFeePolicy());
    Assertions.assertEquals(SentNotificationV25DTO.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER, result.getPhysicalCommunicationType());
    Assertions.assertEquals("Test Notifica Piattaforma Unitaria", request.getSubject());
    Assertions.assertEquals("SENDERDENOMINATION", result.getSenderDenomination());
    Assertions.assertEquals("SENDERTAXID", result.getSenderTaxId());
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

    Organization organization = new Organization();
    organization.setOrganizationId(request.getOrganizationId());

    when(debtPositionServiceMock.findDebtPositionByInstallment(organization.getOrganizationId(), nav, accessToken))
      .thenReturn(null);
    when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.of(organization));

    Assertions.assertThrows(UnknownDebtPositionException.class, () -> mapper.mapToModel(request, null, accessToken));
  }

  @Test
  void givenNoStationWhenMapToModelThenThrow() {
    // Given
    CreateNotificationRequest request = buildRequest();
    String accessToken = "ACCESSTOKEN";

    when(organizationServiceMock.findOrganizationStation(request.getOrganizationId(), null, accessToken))
      .thenReturn(Optional.empty());

    String nav = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getNoticeCode();
    String orgFiscalCode = request.getRecipients().getFirst()
      .getPayments().getFirst().getPagoPa().getCreditorTaxId();
    String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);

    when(organizationServiceMock.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken))
      .thenReturn(Optional.empty());


    Assertions.assertThrows(NotFoundException.class, () -> mapper.mapToModel(request, null, accessToken));
  }

  private CreateNotificationRequest buildRequest() {
    Recipient recipient = buildRecipient();
    Document document = buildDocument();

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setOrganizationId(1L);
    request.setPaProtocolNumber("Prot_001");
    request.setRecipients(Collections.singletonList(recipient));
    request.setDocuments(Collections.singletonList(document));
    request.setNotificationFeePolicy(NotificationFeePolicyDTO.DELIVERY_MODE);
    request.setPhysicalCommunicationType(NewNotificationRequestV25DTO.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER);
    request.setSubject("Test Notifica Piattaforma Unitaria");
    request.setSenderDenomination("SENDERDENOMINATION");
    request.setSenderTaxId("SENDERTAXID");
    request.setAmount(BigDecimal.valueOf(99999999));
    request.setTaxonomyCode("TAXONOMYCODE");
    request.setPaFee(100);
    request.setVat(22);
    request.setPaymentExpirationDate(LocalDate.now());
    request.setExternalCampaignId("CAMPAIGN_ID123");
    request.setSubUnitCode("sub01");
    return request;
  }

  private void checkPayments(DebtPositionDTO debtPosition, SendNotification result) {
    if (debtPosition != null) {
      Assertions.assertSame(debtPosition.getDebtPositionId(), result.getPuRecipients().getFirst().getPuPayments().getFirst().getDebtPositionId());
      Assertions.assertEquals("CREDITORTAXID", result.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().getCreditorTaxId());
      Assertions.assertEquals("NOTICECODE", result.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().getNoticeCode());
      Assertions.assertEquals(true, result.getPuRecipients().getFirst().getPuPayments().getFirst().getPayment().getPagoPa().getApplyCost());
      Assertions.assertEquals(InstallmentStatus.PAID, result.getPuRecipients().getFirst().getPuPayments().getFirst().getStatus());
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
