package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPosition;
import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.connector.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.UnknownDebtPositionException;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CreateNotificationRequest2SendNotificationMapper {

  private final DataCipherService dataCipherService;
  private final DebtPositionService debtPositionService;

  public CreateNotificationRequest2SendNotificationMapper(
    DataCipherService dataCipherService, DebtPositionService debtPositionService) {
    this.dataCipherService = dataCipherService;
    this.debtPositionService = debtPositionService;
  }

  public SendNotificationNoPII mapToNoPII(CreateNotificationRequest request, String accessToken) {
    Long organizationId = request.getOrganizationId();

    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();
    sendNotificationNoPII.setPaProtocolNumber(request.getPaProtocolNumber());
    sendNotificationNoPII.setSubjectType(request.getRecipient().getRecipientType().getValue());
    sendNotificationNoPII.setFiscalCodeHash(dataCipherService.hash(request.getRecipient().getTaxId()));
    sendNotificationNoPII.setDenomination(request.getRecipient().getDenomination());

    if (request.getDocuments().isEmpty()) {
      sendNotificationNoPII.setStatus(NotificationStatus.SENDING);
    }
    else {
      sendNotificationNoPII.setStatus(NotificationStatus.WAITING_FILE);
    }

    sendNotificationNoPII.setPayments(request.getRecipient().getPayments().stream()
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

    sendNotificationNoPII.setDocuments(documents);
    sendNotificationNoPII.setOrganizationId(organizationId);
    sendNotificationNoPII.setNotificationFeePolicy(request.getNotificationFeePolicy().getValue());
    sendNotificationNoPII.setPhysicalCommunicationType(request.getPhysicalCommunicationType().getValue());
    sendNotificationNoPII.setSenderDenomination(request.getSenderDenomination());
    sendNotificationNoPII.setSenderTaxId(request.getSenderTaxId());
    sendNotificationNoPII.setAmount(request.getAmount().intValue());
    sendNotificationNoPII.setTaxonomyCode(request.getTaxonomyCode());
    sendNotificationNoPII.setPaFee(request.getPaFee());
    sendNotificationNoPII.setVat(request.getVat());
    sendNotificationNoPII.setPaymentExpirationDate(request.getPaymentExpirationDate().toString());
    sendNotificationNoPII.setPagoPaIntMode(request.getPagoPaIntMode().getValue());

    return sendNotificationNoPII;
  }

  public SendNotificationPIIDTO mapToPii(CreateNotificationRequest request) {
    SendNotificationPIIDTO pii = new SendNotificationPIIDTO();
    pii.setFiscalCode(request.getRecipient().getTaxId());
    pii.setAddress(request.getRecipient().getPhysicalAddress());
    return pii;
  }
}
