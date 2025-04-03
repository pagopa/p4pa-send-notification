package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
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

import java.time.OffsetDateTime;
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
  private String subjectType;
  private byte[] fiscalCodeHash;
  private String denomination;
  private List<PuPayment> payments;
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
  private OffsetDateTime notificationData;
  @NotNull
  private Long personalDataId;
}
