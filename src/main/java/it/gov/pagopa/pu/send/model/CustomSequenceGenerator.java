package it.gov.pagopa.pu.send.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("custom_sequence")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class CustomSequenceGenerator {

  @Id
  private String id;
  private Long seq;
}
