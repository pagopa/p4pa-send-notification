package it.gov.pagopa.pu.send.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.pu.common.pii.dto.FullEntityPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.dto.pii.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import java.util.List;

import it.gov.pagopa.send.dto.generated.NewNotificationRequestV25DTO;
import it.gov.pagopa.send.dto.generated.NotificationFeePolicyDTO;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
  @Enumerated(EnumType.STRING)
  private NotificationFeePolicyDTO notificationFeePolicy;
  @Enumerated(EnumType.STRING)
  private NewNotificationRequestV25DTO.PhysicalCommunicationTypeEnum physicalCommunicationType;
  private String subject;
  private String senderDenomination;
  private String senderTaxId;
  private int amount;
  private String paymentExpirationDate;
  private String taxonomyCode;
  private String taxonomyDescription;
  private int paFee;
  private int vat;
  private String pagoPaIntMode;
  private List<LegalFactDTO> legalFacts;
  private String campaignId;
  private String orgSubUnitCode;

  @JsonIgnore
  private SendNotificationNoPII noPII;
}
