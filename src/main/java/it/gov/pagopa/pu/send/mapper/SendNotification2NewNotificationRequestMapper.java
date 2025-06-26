package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.Attachment;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.Objects;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SendNotification2NewNotificationRequestMapper {

  public final SendNotificationPIIMapper sendNotificationPIIMapper;

  public SendNotification2NewNotificationRequestMapper(
    SendNotificationPIIMapper sendNotificationPIIMapper) {
    this.sendNotificationPIIMapper = sendNotificationPIIMapper;
  }

  public NewNotificationRequestV24DTO apply(SendNotificationNoPII sendNotificationNoPII) {
    SendNotification sendNotification = sendNotificationPIIMapper.map(sendNotificationNoPII);

    NewNotificationRequestV24DTO newNotification = new NewNotificationRequestV24DTO();
    newNotification.setIdempotenceToken(sendNotification.getSendNotificationId());
    newNotification.setPaProtocolNumber(sendNotification.getPaProtocolNumber());
    newNotification.setSubject("TEST notifica PU numero " + sendNotification.getSendNotificationId());

    newNotification.recipients(setRecipients(sendNotification));
    newNotification.setDocuments(setDocuments(sendNotification));

    //fee domain
    newNotification.setNotificationFeePolicy(NotificationFeePolicyDTO.valueOf(sendNotification.getNotificationFeePolicy()));
    newNotification.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.valueOf(sendNotification.getPhysicalCommunicationType()));
    newNotification.senderDenomination(sendNotification.getSenderDenomination());
    newNotification.senderTaxId(sendNotification.getSenderTaxId());
    newNotification.setTaxonomyCode(sendNotification.getTaxonomyCode());

    if (sendNotification.getAmount() != 0) {
      newNotification.setAmount(sendNotification.getAmount());
    }
    if (sendNotification.getPaFee() != 0) {
      newNotification.setPaFee(sendNotification.getPaFee());
    }
    if (sendNotification.getVat() != 0) {
      newNotification.setVat(sendNotification.getVat());
    }
    if (sendNotification.getPaymentExpirationDate() != null) {
      newNotification.setPaymentExpirationDate(sendNotification.getPaymentExpirationDate());
    }
    if (sendNotification.getPagoPaIntMode() != null) {
      newNotification.setPagoPaIntMode(PagoPaIntModeEnum.valueOf(sendNotification.getPagoPaIntMode()));
    }
    //end fee domain

    return newNotification;
  }

  private List<NotificationRecipientV23DTO> setRecipients(SendNotification sendNotification) {
    return sendNotification.getPuRecipients().stream().map(
      puRecipient -> {
        NotificationRecipientV23DTO notificationRecipient = new NotificationRecipientV23DTO();
        notificationRecipient.setRecipientType(RecipientTypeEnum.valueOf(puRecipient.getRecipient().getRecipientType().getValue()));
        notificationRecipient.taxId(puRecipient.getRecipient().getTaxId());
        notificationRecipient.denomination(puRecipient.getRecipient().getDenomination());

        //address domain
        NotificationPhysicalAddressDTO addressDTO = new NotificationPhysicalAddressDTO();
        addressDTO.setAddress(puRecipient.getRecipient().getPhysicalAddress().getAddress());
        addressDTO.setZip(puRecipient.getRecipient().getPhysicalAddress().getZip());
        addressDTO.setMunicipality(puRecipient.getRecipient().getPhysicalAddress().getMunicipality());
        addressDTO.setProvince(puRecipient.getRecipient().getPhysicalAddress().getProvince());

        notificationRecipient.setPhysicalAddress(addressDTO);
        //end address domain

        //digital address domain
        if (puRecipient.getRecipient().getDigitalDomicile() != null) {
          NotificationDigitalAddressDTO digitalAddressDTO = new NotificationDigitalAddressDTO();
          digitalAddressDTO.setAddress(puRecipient.getRecipient().getDigitalDomicile().getAddress());
          digitalAddressDTO.setType(NotificationDigitalAddressDTO.TypeEnum.valueOf(puRecipient.getRecipient().getDigitalDomicile().getType().getValue()));
          notificationRecipient.digitalDomicile(digitalAddressDTO);
        }
        //end digital address domain

        //payments domain to implements
        List<NotificationPaymentItemDTO> payments = puRecipient.getRecipient().getPayments().stream().map(payment -> {
          PagoPaPaymentDTO pagoPa = new PagoPaPaymentDTO();
          pagoPa.setCreditorTaxId(payment.getPagoPa().getCreditorTaxId());
          pagoPa.setNoticeCode(payment.getPagoPa().getNoticeCode());
          pagoPa.setApplyCost(payment.getPagoPa().getApplyCost());

          Optional<NotificationPaymentAttachmentDTO> attachment = sendNotification.getDocuments().stream()
            .filter(doc -> payment.getPagoPa().getAttachment() != null
              && doc.getFileName().equals(payment.getPagoPa().getAttachment().getFileName()))
            .findFirst()
            .map(doc -> {
              NotificationPaymentAttachmentDTO attachmentDTO = new NotificationPaymentAttachmentDTO();
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

        notificationRecipient.setPayments(payments);
        //end payments

        return notificationRecipient;
      }).toList();
  }

  private List<NotificationDocumentDTO> setDocuments(SendNotification sendNotification) {
    Set<String> attachmentFileNames =
      sendNotification.getPuRecipients().stream()
        .flatMap(r -> r.getPuPayments().stream())
        .map(payment -> payment.getPayment().getPagoPa().getAttachment())
        .filter(Objects::nonNull)
        .map(Attachment::getFileName)
        .collect(Collectors.toSet());

    return sendNotification.getDocuments().stream()
      .filter(doc -> !attachmentFileNames.contains(doc.getFileName()))
      .map(doc -> {
        NotificationDocumentDTO documentDTO = new NotificationDocumentDTO();
        documentDTO.setDigests(new NotificationAttachmentDigestsDTO().sha256(doc.getDigest()));
        documentDTO.setContentType(doc.getContentType());
        documentDTO.ref(new NotificationAttachmentBodyRefDTO()
          .key(doc.getKey())
          .versionToken(doc.getVersionId()));
        return documentDTO;
      }).toList();
  }
}
