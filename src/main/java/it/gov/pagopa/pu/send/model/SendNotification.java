package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
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
  private String id;
  private Long sendNotificationId;
  private String preloadId;
  private String contentType;
  private String expectedFileDigest;
  private NotificationStatus status;
}
