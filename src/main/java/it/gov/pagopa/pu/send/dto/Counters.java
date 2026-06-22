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
private Long total;
private Long accepted;
private Long delivered;
private Long digitalCompleted;
private Long analogicCompleted;
private Long completion;
}
