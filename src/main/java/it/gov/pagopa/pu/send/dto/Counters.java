package it.gov.pagopa.pu.send.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class Counters implements Serializable {
private Long total = 0L;
private Long accepted = 0L;
private Long delivered = 0L;
private Long digitalCompleted = 0L;
private Long analogicCompleted = 0L;
private Long completion = 0L;
}
