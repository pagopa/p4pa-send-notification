package it.gov.pagopa.pu.send.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.pu.common.pii.dto.FullEntityPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.dto.pii.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotification implements FullEntityPIIDTO<SendNotificationNoPII, SendNotificationPIIDTO> {

  private String sendNotificationId;
  private Long organizationId;
  private String paProtocolNumber;
  private List<PuRecipient> puRecipients;
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

  @JsonIgnore
  private SendNotificationNoPII noPII;
}
