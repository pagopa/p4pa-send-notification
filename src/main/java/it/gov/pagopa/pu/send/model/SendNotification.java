package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("send_notification")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class SendNotification {
  @Id
  private String sendNotificationId;
  private Long organizationId;
  private String paProtocolNumber;
  private String subjectType;
  private String fiscalCode;
  private String denomination;
  private List<Payment> payments;
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
}
