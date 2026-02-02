package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationFeePolicyDTO;
import it.gov.pagopa.pu.send.dto.LegalFactDTO;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.enums.NotificationStatus;

import java.util.List;

import static it.gov.pagopa.pu.send.util.faker.DocumentFaker.buildDocumentAttachment;
import static it.gov.pagopa.pu.send.util.faker.DocumentFaker.buildDocumentDTO;
import static it.gov.pagopa.pu.send.util.faker.PuRecipientFaker.buildPuRecipient;

public class SendNotificationFaker {

  public static SendNotification buildSendNotification() {
    return SendNotification.builder()
      .sendNotificationId("12345")
      .organizationId(2L)
      .status(NotificationStatus.WAITING_FILE)
      .notificationRequestId("REQ001")
      .iun("IUN123")
      .paProtocolNumber("Prot_001")
      .documents(List.of(buildDocumentAttachment(), buildDocumentDTO()))
      .notificationFeePolicy(NotificationFeePolicyDTO.DELIVERY_MODE.getValue())
      .physicalCommunicationType(NewNotificationRequestV24DTO.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER.getValue())
      .senderDenomination("Ente Intermediario 2")
      .senderTaxId("00000000018")
      .amount(100)
      .taxonomyCode("010101P")
      .paFee(100)
      .vat(22)
      .paymentExpirationDate("2025-12-31")
      .puRecipients(List.of(buildPuRecipient()))
      .legalFacts(List.of(LegalFactDTO.builder()
        .fileName("fileName")
        .url("url")
        .category(LegalFactCategoryDTO.ANALOG_DELIVERY)
        .build()))
      .build();
  }
}
