package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationAttachmentBodyRefDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationAttachmentDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationAttachmentDigestsDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationDocumentDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationFeePolicyDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPaymentItemDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPhysicalAddressDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PagoPaPaymentDTO;
import it.gov.pagopa.pu.send.model.SendNotification;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SendNotification2NewNotificationRequestMapper {

  public NewNotificationRequestV24DTO apply(SendNotification sendNotification) {
    //TODO some information (only required) are mocked, other information will be implemented with P4ADEV-2185
    NewNotificationRequestV24DTO newNotification = new NewNotificationRequestV24DTO();
    newNotification.setIdempotenceToken(sendNotification.getSendNotificationId());
    newNotification.setPaProtocolNumber(sendNotification.getPaProtocolNumber());
    newNotification.setSubject("TEST notifica PU numero "+sendNotification.getSendNotificationId());

    NotificationRecipientV23DTO recipient = new NotificationRecipientV23DTO();
    recipient.setRecipientType(RecipientTypeEnum.valueOf(sendNotification.getSubjectType()));
    recipient.taxId(sendNotification.getFiscalCode());
    recipient.denomination("Michelangelo Buonarroti");

    //address domain
    NotificationPhysicalAddressDTO addressDTO = new NotificationPhysicalAddressDTO();
    addressDTO.setAddress("Via Larga 10");
    addressDTO.setZip("00186");
    addressDTO.setMunicipality("Roma");
    addressDTO.setProvince("RM");
    recipient.setPhysicalAddress(addressDTO);
    //end address domain

    //payments domain to implements
    List<NotificationPaymentItemDTO> payments = sendNotification.getPayments().stream().map(payment -> {
      PagoPaPaymentDTO pagoPa = new PagoPaPaymentDTO();
      pagoPa.setCreditorTaxId(payment.getPagoPa().getCreditorTaxId());
      pagoPa.setNoticeCode(payment.getPagoPa().getNoticeCode());
      pagoPa.setApplyCost(payment.getPagoPa().getApplyCost());

      Optional<NotificationAttachmentDTO> attachment = sendNotification.getDocuments().stream()
          .filter(doc -> doc.getFileName().equals(payment.getPagoPa().getAttachment().getFileName()))
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
      .map(payment -> payment.getPagoPa().getAttachment().getFileName())
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
    newNotification.setNotificationFeePolicy(NotificationFeePolicyDTO.valueOf("DELIVERY_MODE"));
    newNotification.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.valueOf("AR_REGISTERED_LETTER"));
    newNotification.senderDenomination("Ente Intermediario 2");
    newNotification.senderTaxId("00000000018");
    newNotification.setTaxonomyCode("010101P");
    newNotification.setPaFee(100);
    newNotification.setVat(22);
    //end fee domain

    newNotification.recipients(List.of(recipient));
    return newNotification;
  }
}
