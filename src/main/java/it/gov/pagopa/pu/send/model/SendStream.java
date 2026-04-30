package it.gov.pagopa.pu.send.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("send_stream")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class SendStream extends BaseEntity {
  @Id
  private String streamId;
  @NotNull
  private Long organizationId;
  @NotNull
  private String title;
  @NotNull
  private String eventType;
  private String lastEventId;
}
