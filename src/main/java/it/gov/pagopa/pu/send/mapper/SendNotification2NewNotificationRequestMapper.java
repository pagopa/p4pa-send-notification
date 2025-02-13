package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationAttachmentBodyRefDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationAttachmentDigestsDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationDocumentDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationFeePolicyDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPhysicalAddressDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationRecipientV23DTO.RecipientTypeEnum;
import it.gov.pagopa.pu.send.model.SendNotification;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SendNotification2NewNotificationRequestMapper {

  public NewNotificationRequestV24DTO apply(SendNotification sendNotification) {
    //TODO some information (only required) are mocked, other information will be implemented with P4DEV-
    NewNotificationRequestV24DTO newNotification = new NewNotificationRequestV24DTO();
    newNotification.setIdempotenceToken(sendNotification.getSendNotificationId());
    newNotification.setPaProtocolNumber(sendNotification.getPaProtocolNumber());
    newNotification.setSubject(sendNotification.getSendNotificationId());

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
    //end payments

    //documents domain
    List<NotificationDocumentDTO> documents = sendNotification.getDocuments().stream().map(doc -> {
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
    newNotification.setNotificationFeePolicy(NotificationFeePolicyDTO.valueOf("FLAT_RATE"));
    newNotification.setPhysicalCommunicationType(PhysicalCommunicationTypeEnum.valueOf("AR_REGISTERED_LETTER"));
    newNotification.senderDenomination("SIL");
    newNotification.senderTaxId("00000000018");
    newNotification.setTaxonomyCode("010101P");
    //end fee domain

    newNotification.recipients(List.of(recipient));
    return newNotification;
  }
}
