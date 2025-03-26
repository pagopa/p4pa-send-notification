package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.send.connector.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.UnknownDebtPositionException;
import it.gov.pagopa.pu.send.model.SendNotification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CreateNotificationRequest2SendNotificationMapper {

  private final DebtPositionService debtPositionService;

  public CreateNotificationRequest2SendNotificationMapper(DebtPositionService debtPositionService) {
    this.debtPositionService = debtPositionService;
  }

  public SendNotification map(CreateNotificationRequest request, String accessToken) {
    Long organizationId = request.getOrganizationId();

    SendNotification sendNotification = new SendNotification();
    sendNotification.setPaProtocolNumber(request.getPaProtocolNumber());
    sendNotification.setSubjectType(request.getRecipient().getRecipientType().getValue());
    sendNotification.setFiscalCode(request.getRecipient().getTaxId());
    sendNotification.setDenomination(request.getRecipient().getDenomination());

    if (request.getDocuments().isEmpty()) {
      sendNotification.setStatus(NotificationStatus.SENDING);
    }
    else {
      sendNotification.setStatus(NotificationStatus.WAITING_FILE);
    }

    sendNotification.setPayments(request.getRecipient().getPayments().stream()
      .map(p -> {
        String nav = p.getPagoPa().getNoticeCode();
        DebtPosition debtPosition = debtPositionService.findDebtPositionByInstallment(organizationId, nav, accessToken);
        if (debtPosition == null) {
          throw new UnknownDebtPositionException("Cannot find debtPosition related to organizationId " + organizationId + " and having an Installment with NAV " + nav);
        } else {
          return new PuPayment(debtPosition.getDebtPositionId(), p);
        }
      })
      .toList()
    );

    List<DocumentDTO> documents = new ArrayList<>();
    // add attachment to documents
    documents.addAll(request.getRecipient().getPayments().stream()
      .map(payment ->
        DocumentDTO.builder()
          .fileName(payment.getPagoPa().getAttachment().getFileName())
          .contentType(payment.getPagoPa().getAttachment().getContentType())
          .digest(payment.getPagoPa().getAttachment().getDigest())
          .status(FileStatus.WAITING)
          .build()).toList());

    // set documents
    documents.addAll(request.getDocuments().stream()
      .map(document ->
        DocumentDTO.builder()
          .fileName(document.getFileName())
          .contentType(document.getContentType())
          .digest(document.getDigest())
          .status(FileStatus.WAITING)
          .build()).toList());

    sendNotification.setDocuments(documents);
    sendNotification.setOrganizationId(organizationId);
    sendNotification.setNotificationFeePolicy(request.getNotificationFeePolicy().getValue());
    sendNotification.setPhysicalCommunicationType(request.getPhysicalCommunicationType().getValue());
    sendNotification.setSenderDenomination(request.getSenderDenomination());
    sendNotification.setSenderTaxId(request.getSenderTaxId());
    sendNotification.setAmount(request.getAmount().intValue());
    sendNotification.setTaxonomyCode(request.getTaxonomyCode());
    sendNotification.setPaFee(request.getPaFee());
    sendNotification.setVat(request.getVat());
    sendNotification.setPaymentExpirationDate(request.getPaymentExpirationDate().toString());
    sendNotification.setPagoPaIntMode(request.getPagoPaIntMode().getValue());

    return sendNotification;
  }
}
