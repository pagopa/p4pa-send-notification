package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.dto.pii.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.send.dto.generated.NotificationFeePolicyDTO;
import it.gov.pagopa.send.dto.generated.NotificationRefusedErrorV27DTO;
import it.gov.pagopa.send.dto.generated.SentNotificationV25DTO;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = false)
public class SendNotificationNoPII extends BaseEntity implements NoPIIEntity<SendNotificationPIIDTO> {
  @Id
  private String sendNotificationId;
  private Long organizationId;
  private String orgSubUnitCode;
  @NotNull
  private String campaignId;
  private String paProtocolNumber;
  private List<PuRecipientNoPIIDTO> recipients;
  private List<DocumentDTO> documents;
  private NotificationStatus status;
  private List<StreamEventSummaryDTO> history;
  private String notificationRequestId;
  private String iun;
  @Enumerated(EnumType.STRING)
  private NotificationFeePolicyDTO notificationFeePolicy;
  @Enumerated(EnumType.STRING)
  private SentNotificationV25DTO.PhysicalCommunicationTypeEnum physicalCommunicationType;
  private String subject;
  private String senderDenomination;
  private String senderTaxId;
  private int amount;
  private String paymentExpirationDate;
  private String taxonomyCode;
  private int paFee;
  private int vat;
  private String pagoPaIntMode;
  private List<LegalFactDTO> legalFacts;
  private List<NotificationRefusedErrorV27DTO> refusalReasons;
  @NotNull
  private Long personalDataId;
}
