package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SendNotification2NewNotificationRequestMapper {

  public NewNotificationRequestV24DTO apply(
    SendNotificationNoPII sendNotificationNoPII) {
    //TODO some information (only required) are mocked, other information will be implemented with P4ADEV-2185
    NewNotificationRequestV24DTO newNotification = new NewNotificationRequestV24DTO();
    newNotification.setIdempotenceToken(sendNotificationNoPII.getSendNotificationId());
    newNotification.setPaProtocolNumber(sendNotificationNoPII.getPaProtocolNumber());
    newNotification.setSubject("TEST notifica PU numero "+ sendNotificationNoPII.getSendNotificationId());

    NotificationRecipientV23DTO recipient = new NotificationRecipientV23DTO();
    recipient.setRecipientType(RecipientTypeEnum.valueOf(sendNotificationNoPII.getSubjectType()));
    //recipient.taxId(sendNotificationNoPII.getFiscalCode()); prendere nopii
    recipient.denomination(sendNotificationNoPII.getDenomination());

    //address domain
    NotificationPhysicalAddressDTO addressDTO = new NotificationPhysicalAddressDTO();
    addressDTO.setAddress("Via Larga 10");
    addressDTO.setZip("00186");
    addressDTO.setMunicipality("Roma");
    addressDTO.setProvince("RM");
    recipient.setPhysicalAddress(addressDTO);
    //end address domain

    //payments domain to implements
    List<NotificationPaymentItemDTO> payments = sendNotificationNoPII.getPayments().stream().map(payment -> {
      PagoPaPaymentDTO pagoPa = new PagoPaPaymentDTO();
      pagoPa.setCreditorTaxId(payment.getPayment().getPagoPa().getCreditorTaxId());
      pagoPa.setNoticeCode(payment.getPayment().getPagoPa().getNoticeCode());
      pagoPa.setApplyCost(payment.getPayment().getPagoPa().getApplyCost());

      Optional<NotificationAttachmentDTO> attachment = sendNotificationNoPII.getDocuments().stream()
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

    Set<String> attachmentFileNames = sendNotificationNoPII.getPayments().stream()
      .filter(payment -> payment.getPayment().getPagoPa().getAttachment() != null)
      .map(payment -> payment.getPayment().getPagoPa().getAttachment().getFileName())
      .collect(Collectors.toSet());
    //end payments

    //documents domain
    List<NotificationDocumentDTO> documents = sendNotificationNoPII.getDocuments().stream()
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
      sendNotificationNoPII.getNotificationFeePolicy()));
    newNotification.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.valueOf(
      sendNotificationNoPII.getPhysicalCommunicationType()));
    newNotification.senderDenomination(sendNotificationNoPII.getSenderDenomination());
    newNotification.senderTaxId(sendNotificationNoPII.getSenderTaxId());
    if(sendNotificationNoPII.getAmount()!=0)
      newNotification.setAmount(sendNotificationNoPII.getAmount());
    newNotification.setTaxonomyCode(sendNotificationNoPII.getTaxonomyCode());
    if(sendNotificationNoPII.getPaFee()!=0)
      newNotification.setPaFee(sendNotificationNoPII.getPaFee());
    if(sendNotificationNoPII.getVat()!=0)
      newNotification.setVat(sendNotificationNoPII.getVat());
    if(sendNotificationNoPII.getPaymentExpirationDate()!=null)
      newNotification.setPaymentExpirationDate(sendNotificationNoPII.getPaymentExpirationDate());
    if(sendNotificationNoPII.getPagoPaIntMode()!=null)
      newNotification.setPagoPaIntMode(PagoPaIntModeEnum.valueOf(
        sendNotificationNoPII.getPagoPaIntMode()));
    //end fee domain

    newNotification.recipients(List.of(recipient));
    return newNotification;
  }
}
