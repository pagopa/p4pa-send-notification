package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("send_notification")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
public class SendNotificationNoPII implements Serializable, NoPIIEntity<SendNotificationPIIDTO> {
  @Id
  private String sendNotificationId;
  private Long organizationId;
  private String paProtocolNumber;
  private List<PuRecipientNoPIIDTO> recipients;
  private List<DocumentDTO> documents;
  private NotificationStatus status;
  private String notificationRequestId;
  private String iun;
  private String notificationFeePolicy;
  private String physicalCommunicationType;
  private String senderDenomination;
  private String senderTaxId;
  private int amount;
  private String paymentExpirationDate;
  private String taxonomyCode;
  private int paFee;
  private int vat;
  private String pagoPaIntMode;
  private List<LegalFactDTO> legalFacts;
  @NotNull
  private Long personalDataId;
}
