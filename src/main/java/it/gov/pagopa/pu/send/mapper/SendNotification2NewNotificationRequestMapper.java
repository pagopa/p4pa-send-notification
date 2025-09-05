package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PagoPaIntModeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipient;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.Attachment;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    newNotification.setSubject("Notifica Piattaforma Unitaria");

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
        List<NotificationPaymentItemDTO> payments = getPayments(sendNotification, puRecipient);
        notificationRecipient.setPayments(payments);
        //end payments

        return notificationRecipient;
      }).toList();
  }

  private static List<NotificationPaymentItemDTO> getPayments(SendNotification sendNotification, PuRecipient puRecipient) {
    return puRecipient.getPuPayments().stream().map(puPayment -> {
      PagoPaPaymentDTO pagoPa = new PagoPaPaymentDTO();
      pagoPa.setCreditorTaxId(puPayment.getPayment().getPagoPa().getCreditorTaxId());
      pagoPa.setNoticeCode(puPayment.getPayment().getPagoPa().getNoticeCode());
      pagoPa.setApplyCost(puPayment.getPayment().getPagoPa().getApplyCost());

      Optional<NotificationPaymentAttachmentDTO> attachment = getAttachment(sendNotification, puPayment);
      attachment.ifPresent(pagoPa::setAttachment);

      F24PaymentDTO f24Payment = null;
      if (puPayment.getPayment().getF24() != null) {
        f24Payment = new F24PaymentDTO();
        f24Payment.setTitle(puPayment.getPayment().getF24().getTitle());
        f24Payment.setApplyCost(puPayment.getPayment().getF24().getApplyCost());
        Optional<NotificationMetadataAttachmentDTO> metadataAttachment = getMetadataAttachment(sendNotification, puPayment);
        metadataAttachment.ifPresent(f24Payment::setMetadataAttachment);
      }

      return new NotificationPaymentItemDTO()
        .pagoPa(pagoPa)
        .f24(f24Payment);
    }).toList();
  }

  private static Optional<NotificationPaymentAttachmentDTO> getAttachment(SendNotification sendNotification, PuPayment puPayment) {
    return sendNotification.getDocuments().stream()
      .filter(doc -> puPayment.getPayment().getPagoPa().getAttachment() != null
        && doc.getFileName().equals(puPayment.getPayment().getPagoPa().getAttachment().getFileName()))
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
  }

  private static Optional<NotificationMetadataAttachmentDTO> getMetadataAttachment(SendNotification sendNotification, PuPayment puPayment) {
    return sendNotification.getDocuments().stream()
      .filter(doc -> puPayment.getPayment().getF24().getMetadataAttachment() != null
        && doc.getFileName().equals(puPayment.getPayment().getF24().getMetadataAttachment().getFileName()))
      .findFirst()
      .map(doc -> {
        NotificationMetadataAttachmentDTO attachmentDTO = new NotificationMetadataAttachmentDTO();
        attachmentDTO.setContentType(doc.getContentType());
        attachmentDTO.setDigests(new NotificationAttachmentDigestsDTO().sha256(doc.getDigest()));
        attachmentDTO.setRef(new NotificationAttachmentBodyRefDTO()
          .key(doc.getKey())
          .versionToken(doc.getVersionId()));
        return attachmentDTO;
      });
  }

  private List<NotificationDocumentDTO> setDocuments(SendNotification sendNotification) {
    Set<String> attachmentFileNames = sendNotification.getPuRecipients().stream()
      .flatMap(r -> r.getPuPayments().stream())
      .flatMap(puPayment -> {
        Payment payment = puPayment.getPayment();
        return Stream.of(
          payment.getPagoPa().getAttachment(),
          payment.getF24() != null ? payment.getF24().getMetadataAttachment() : null
        );
      })
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
      })
      .toList();
  }

}
