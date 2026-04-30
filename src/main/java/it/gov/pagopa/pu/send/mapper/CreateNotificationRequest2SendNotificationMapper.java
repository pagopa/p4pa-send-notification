package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.send.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipient;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.Recipient;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.UnknownDebtPositionException;
import it.gov.pagopa.pu.send.util.DebtPositionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class CreateNotificationRequest2SendNotificationMapper {

  private final DebtPositionService debtPositionService;
  private final BrokerService brokerService;
  private final OrganizationService organizationService;

  public CreateNotificationRequest2SendNotificationMapper(
    DebtPositionService debtPositionService,
    BrokerService brokerService,
    OrganizationService organizationService
  ) {
    this.debtPositionService = debtPositionService;
    this.brokerService = brokerService;
    this.organizationService = organizationService;
  }

  public SendNotification mapToModel(CreateNotificationRequest request, String accessToken) {
    Long organizationId = request.getOrganizationId();

    SendNotification sendNotification = new SendNotification();

    sendNotification.setPaProtocolNumber(request.getPaProtocolNumber());

    if (request.getDocuments().isEmpty()) {
      sendNotification.setStatus(NotificationStatus.SENDING);
    } else {
      sendNotification.setStatus(NotificationStatus.WAITING_FILE);
    }

    sendNotification.setPuRecipients(setPuRecipients(request, accessToken));
    sendNotification.setDocuments(setDocuments(request));

    sendNotification.setOrganizationId(organizationId);
    sendNotification.setNotificationFeePolicy(request.getNotificationFeePolicy().getValue());
    sendNotification.setPhysicalCommunicationType(request.getPhysicalCommunicationType().getValue());
    sendNotification.setSenderDenomination(request.getSenderDenomination());
    sendNotification.setSenderTaxId(request.getSenderTaxId());
    sendNotification.setTaxonomyCode(request.getTaxonomyCode());
    if (request.getAmount() != null) {
      sendNotification.setAmount(request.getAmount().intValue());
    }
    if (request.getPaFee() != null) {
      sendNotification.setPaFee(request.getPaFee());
    }
    if (request.getVat() != null) {
      sendNotification.setVat(request.getVat());
    }
    if (request.getPaymentExpirationDate() != null) {
      sendNotification.setPaymentExpirationDate(request.getPaymentExpirationDate().toString());
    }

    Broker broker = brokerService.getBrokerByOrganizationId(organizationId, accessToken);
    sendNotification.setPagoPaIntMode(
      broker.getPagoPaInteractionModel().getValue().contains("ASYNC")
      ? "ASYNC"
      : "SYNC");

    return sendNotification;
  }

  private List<PuRecipient> setPuRecipients(CreateNotificationRequest request, String accessToken) {
    return request.getRecipients().stream()
      .map(r -> {
        List<PuPayment> puPayments = r.getPayments().stream()
          .map(p -> getPuPayment(accessToken, p)).toList();
        Recipient recipient = Recipient.builder()
          .recipientType(r.getRecipientType())
          .taxId(r.getTaxId())
          .denomination(r.getDenomination())
          .physicalAddress(r.getPhysicalAddress())
          .digitalDomicile(r.getDigitalDomicile())
          .build();
        return new PuRecipient(recipient, puPayments);
      }).toList();
  }

  private PuPayment getPuPayment(String accessToken, Payment p) {
    if (p.getPagoPa() != null) {
      String orgFiscalCode = p.getPagoPa().getCreditorTaxId();
      String nav = p.getPagoPa().getNoticeCode();
      String segregationCode = DebtPositionUtils.extractSegregationCodeFromNav(nav);
      Optional<Organization> optionalOrganizationManagedByPU =
        organizationService.findByOrgFiscalCodeAndSegregationCode(orgFiscalCode, segregationCode, accessToken);
      return optionalOrganizationManagedByPU
        .map(Organization::getOrganizationId)
        .map(organizationId -> getPuPaymentWithDebtPositionId(p, nav, organizationId, accessToken))
        .orElseGet(() -> new PuPayment(null, p, null));
    }
    if (p.getF24() != null) {
      return new PuPayment(null, p, null);
    }
    return null;
  }

  private PuPayment getPuPaymentWithDebtPositionId(Payment p, String nav, Long organizationId, String accessToken) {
    DebtPositionDTO debtPosition = debtPositionService.findDebtPositionByInstallment(organizationId, nav, accessToken);
    if (debtPosition == null) {
      throw new UnknownDebtPositionException("Cannot find debtPosition related to organizationId " + organizationId + " and having an Installment with NAV " + nav);
    } else {
      return new PuPayment(debtPosition.getDebtPositionId(), p, null);
    }
  }

  private List<DocumentDTO> setDocuments(CreateNotificationRequest request) {
    List<DocumentDTO> documents = new ArrayList<>();

    documents.addAll(request.getRecipients().stream()
      .flatMap(r -> r.getPayments().stream())
      .flatMap(p -> Stream.of(
        p.getPagoPa() != null ? p.getPagoPa().getAttachment() : null,
        p.getF24() != null ? p.getF24().getMetadataAttachment() : null
      ))
      .filter(Objects::nonNull)
      .map(attachment -> DocumentDTO.builder()
        .fileName(attachment.getFileName())
        .contentType(attachment.getContentType())
        .digest(attachment.getDigest())
        .status(FileStatus.WAITING)
        .build())
      .toList());

    documents.addAll(request.getDocuments().stream()
      .map(document -> DocumentDTO.builder()
        .fileName(document.getFileName())
        .contentType(document.getContentType())
        .digest(document.getDigest())
        .status(FileStatus.WAITING)
        .build())
      .toList());

    return documents;
  }
}
