package it.gov.pagopa.pu.send.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
  private Long failedFileNotFound = 0L;
  private Long failedFileGone = 0L;
  private Long failedFileShaError = 0L;
  private Long failedFilePdfInvalidError = 0L;
  private Long failedFilePdfTooBigError = 0L;
  private Long failedF24MetadataNotValid = 0L;
  private Long failedTaxIdNotValid = 0L;
  private Long failedRecipientIdNotValid = 0L;
  private Long failedNotValidAddress = 0L;
  private Long failedPaymentNotValid = 0L;
  private Long failedServiceUnavailable = 0L;
  private Long failedAddressNotFound = 0L;
  private Long failedAddressSearchFailed = 0L;
  private Long deceasedRecipient = 0L;
  private Long recipientNotFound = 0L;
  private Long paid = 0L;
  private OffsetDateTime fullRecalculationDate;
}
