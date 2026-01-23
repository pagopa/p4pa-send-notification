package it.gov.pagopa.pu.send.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document("send_stream")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
public class SendStream implements Serializable {
  @Id
  private String streamId;
  @NotNull
  private String organizationIpaCode;
  @NotNull
  private String title;
  @NotNull
  private String eventType;
  private String lastEventId;
}
