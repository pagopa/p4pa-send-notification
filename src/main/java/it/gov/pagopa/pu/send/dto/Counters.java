package it.gov.pagopa.pu.send.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class Counters implements Serializable {
  private Long total = 0L;
  private Long accepted = 0L;
  private Long delivered = 0L;
  private Long completed = 0L;
  private Long analogicCompletionPreOutcome = 0L;
  private Long analogicCompletion = 0L;
  private Long digitalCompletionDigitalDomicile = 0L;
  private Long digitalCompletionCourtesyMessage = 0L;
  private Long failed = 0L;
  private Long deceasedRecipient = 0L;
  private Long recipientNotFound = 0L;
  private OffsetDateTime fullRecalculationDate;
  @JsonIgnore
  private LocalDate startDate;
  @JsonIgnore
  private LocalDate endDate;
}

