package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SendNotification2NewNotificationRequestMapper {

  public final SendNotificationMapper sendNotificationMapper;

  public SendNotification2NewNotificationRequestMapper(
    SendNotificationMapper sendNotificationMapper) {
    this.sendNotificationMapper = sendNotificationMapper;
  }

  public NewNotificationRequestV24DTO apply(SendNotificationNoPII sendNotificationNoPII) {

    SendNotification sendNotification = sendNotificationMapper.map(sendNotificationNoPII);

    NewNotificationRequestV24DTO newNotification = new NewNotificationRequestV24DTO();
    newNotification.setIdempotenceToken(sendNotification.getSendNotificationId());
    newNotification.setPaProtocolNumber(sendNotification.getPaProtocolNumber());
    newNotification.setSubject("TEST notifica PU numero "+ sendNotification.getSendNotificationId());

    NotificationRecipientV23DTO recipient = new NotificationRecipientV23DTO();
    recipient.setRecipientType(RecipientTypeEnum.valueOf(sendNotification.getSubjectType()));
    recipient.taxId(sendNotification.getFiscalCode());
    recipient.denomination(sendNotification.getDenomination());

    //address domain
    NotificationPhysicalAddressDTO addressDTO = new NotificationPhysicalAddressDTO();
    addressDTO.setAddress(sendNotification.getAddress().getAddress());
    addressDTO.setZip(sendNotification.getAddress().getZip());
    addressDTO.setMunicipality(sendNotification.getAddress().getMunicipality());
    addressDTO.setProvince(sendNotification.getAddress().getProvince());
    recipient.setPhysicalAddress(addressDTO);
    //end address domain

    //payments domain to implements
    List<NotificationPaymentItemDTO> payments = sendNotification.getPayments().stream().map(payment -> {
      PagoPaPaymentDTO pagoPa = new PagoPaPaymentDTO();
      pagoPa.setCreditorTaxId(payment.getPayment().getPagoPa().getCreditorTaxId());
      pagoPa.setNoticeCode(payment.getPayment().getPagoPa().getNoticeCode());
      pagoPa.setApplyCost(payment.getPayment().getPagoPa().getApplyCost());

      Optional<NotificationAttachmentDTO> attachment = sendNotification.getDocuments().stream()
          .filter(doc -> payment.getPayment().getPagoPa().getAttachment()!=null
            && doc.getFileName().equals(payment.getPayment().getPagoPa().getAttachment().getFileName()))
          .findFirst()
          .map(doc -> {
            NotificationAttachmentDTO attachmentDTO = new NotificationAttachmentDTO();
            attachmentDTO.setContentType(doc.getContentType());
            attachmentDTO.setDigests(new NotificationAttachmentDigestsDTO().sha256(doc.getDigest()));
            attachmentDTO.setRef(new NotificationAttachmentBodyRefDTO()
              .key(doc.getKey())
              .versionToken(doc.getVersionId()));
            return attachmentDTO;
          });
      attachment.ifPresent(pagoPa::setAttachment);
      return new NotificationPaymentItemDTO().pagoPa(pagoPa);
    }).toList();

    recipient.setPayments(payments);

    Set<String> attachmentFileNames = sendNotification.getPayments().stream()
      .filter(payment -> payment.getPayment().getPagoPa().getAttachment() != null)
      .map(payment -> payment.getPayment().getPagoPa().getAttachment().getFileName())
      .collect(Collectors.toSet());
    //end payments

    //documents domain
    List<NotificationDocumentDTO> documents = sendNotification.getDocuments().stream()
      .filter(doc -> !attachmentFileNames.contains(doc.getFileName()))
      .map(doc -> {
      NotificationDocumentDTO documentDTO = new NotificationDocumentDTO();
      documentDTO.setDigests(new NotificationAttachmentDigestsDTO().sha256(doc.getDigest()));
      documentDTO.setContentType(doc.getContentType());
      documentDTO.ref(new NotificationAttachmentBodyRefDTO()
        .key(doc.getKey())
        .versionToken(doc.getVersionId()));
      return  documentDTO;
    }).toList();

    newNotification.setDocuments(documents);
    //end documents

    //fee domain
    newNotification.setNotificationFeePolicy(NotificationFeePolicyDTO.valueOf(
      sendNotification.getNotificationFeePolicy()));
    newNotification.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.valueOf(
      sendNotification.getPhysicalCommunicationType()));
    newNotification.senderDenomination(sendNotification.getSenderDenomination());
    newNotification.senderTaxId(sendNotification.getSenderTaxId());
    if(sendNotification.getAmount()!=0)
      newNotification.setAmount(sendNotification.getAmount());
    newNotification.setTaxonomyCode(sendNotification.getTaxonomyCode());
    if(sendNotification.getPaFee()!=0)
      newNotification.setPaFee(sendNotification.getPaFee());
    if(sendNotification.getVat()!=0)
      newNotification.setVat(sendNotification.getVat());
    if(sendNotification.getPaymentExpirationDate()!=null)
      newNotification.setPaymentExpirationDate(sendNotification.getPaymentExpirationDate());
    if(sendNotification.getPagoPaIntMode()!=null)
      newNotification.setPagoPaIntMode(PagoPaIntModeEnum.valueOf(
        sendNotification.getPagoPaIntMode()));
    //end fee domain

    newNotification.recipients(List.of(recipient));
    return newNotification;
  }
}
