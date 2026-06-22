package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.Counters;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document("campaign")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class Campaign extends BaseEntity {
  @Id
  private String campaignId;
  @NotNull
  private String externalId;
  private String campaignName;
  @NotNull
  private Long organizationId;
  private String orgSubUnitCode;
  private Counters counters;
  private LocalDate startDate;
  private LocalDate endDate;
}
