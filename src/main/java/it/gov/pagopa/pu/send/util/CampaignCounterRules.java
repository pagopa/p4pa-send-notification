package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.send.dto.generated.NotificationRequestRefusedProblemErrorDTO;
import it.gov.pagopa.send.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.send.dto.generated.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import lombok.*;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CampaignCounterRules {
  private CampaignCounterRules() {}

  @Getter
  @Builder
  public static class CounterRule {
    // Ignored when activationPredicate and activationExpressionSupplier are configured, as they take precedence and replace these conditions
    @Builder.Default
    private List<StreamEventSummaryDTO> activationConditions = List.of();
    @Builder.Default
    private List<String> deactivatingCounters = List.of();

    /**
     * Java check of the activation logic, equivalent to the Mongo expression built by {@link #activationExpressionSupplier}.
     * Takes precedence over and replaces {@link #activationConditions}.
     * Configure both callbacks only when activation cannot be expressed through activationConditions.
     */
    private Predicate<SendNotificationNoPII> activationPredicate;

    /**
     * Mongo aggregation expression of the activation logic, equivalent to the check performed by {@link #activationPredicate}.
     * Takes precedence over and replaces {@link #activationConditions}.
     */
    private Supplier<AggregationExpression> activationExpressionSupplier;

    // To be configured if deactivation logic is needed and a deactivation counter is missing, otherwise configuring the counter itself is preferred
    @Builder.Default
    private List<StreamEventSummaryDTO> deactivationConditions = List.of();
  }

  private static final List<String> TERMINAL_COUNTERS = List.of(
    Counters.Fields.completed,
    Counters.Fields.deceasedRecipient,
    Counters.Fields.recipientNotFound
  );

  private static List<String> withTerminalCounters(String... additionalCounters) {
    return Stream.concat(
      TERMINAL_COUNTERS.stream(),
      Arrays.stream(additionalCounters)
    ).toList();
  }

  private static CounterRule failedByRefusalCode(String refusalCode) {
    return CounterRule.builder()
      .activationPredicate(notification -> {
        List<NotificationRequestRefusedProblemErrorDTO> errors = notification.getErrors();
        return errors != null && !errors.isEmpty() && refusalCode.equals(errors.getFirst().getCode());
      })
      .activationExpressionSupplier(() ->
        ComparisonOperators.Eq.valueOf(
          ArrayOperators.ArrayElemAt.arrayOf(SendNotificationNoPII.Fields.errors + ".code").elementAt(0)
        ).equalToValue(refusalCode))
      .build();
  }

  public static final Map<String, CounterRule> COUNTER_RULES = Map.ofEntries(
    Map.entry(Counters.Fields.accepted, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, null)
      ))
      .build()
    ),

    Map.entry(Counters.Fields.delivered, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, null)
      ))
      .build()
    ),

    Map.entry(Counters.Fields.completed, CounterRule.builder()
      .activationConditions(
        List.of(new StreamEventSummaryDTO(NotificationStatusV26DTO.EFFECTIVE_DATE, null))
      )
      .build()
    ),

    Map.entry(Counters.Fields.analogicCompletionPreOutcome, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK)
      ))
      .deactivatingCounters(withTerminalCounters(Counters.Fields.analogicCompletion))
      .build()
    ),

    Map.entry(Counters.Fields.analogicCompletion, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.ANALOG_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW)
      ))
      .deactivatingCounters(TERMINAL_COUNTERS)
      .build()
    ),

    Map.entry(Counters.Fields.digitalCompletionDigitalDomicile, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.DIGITAL_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_SIMPLE_REGISTERED_LETTER),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW)
      ))
      .deactivatingCounters(TERMINAL_COUNTERS)
      .build()
    ),

    Map.entry(Counters.Fields.digitalCompletionCourtesyMessage, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.PROBABLE_SCHEDULING_ANALOG_DATE),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SCHEDULE_ANALOG_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_COURTESY_MESSAGE),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.VIEWED, TimelineElementCategoryV27DTO.NOTIFICATION_VIEWED)
      ))
      // if it's not completed via courtesy message, the notification moves to a digital or analog domicile
      .deactivatingCounters(withTerminalCounters(Counters.Fields.digitalCompletionDigitalDomicile, Counters.Fields.analogicCompletion))
      .build()
    ),

    Map.entry(Counters.Fields.failed, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.REFUSED, null)
      ))
      .build()
    ),

    Map.entry(Counters.Fields.failedFileNotFound, failedByRefusalCode(SendRefusalReasonsCode.FILE_NOTFOUND)),
    Map.entry(Counters.Fields.failedFileGone, failedByRefusalCode(SendRefusalReasonsCode.FILE_GONE)),
    Map.entry(Counters.Fields.failedFileShaError, failedByRefusalCode(SendRefusalReasonsCode.FILE_SHA_ERROR)),
    Map.entry(Counters.Fields.failedFilePdfInvalidError, failedByRefusalCode(SendRefusalReasonsCode.FILE_PDF_INVALID_ERROR)),
    Map.entry(Counters.Fields.failedFilePdfTooBigError, failedByRefusalCode(SendRefusalReasonsCode.FILE_PDF_TOOBIG_ERROR)),
    Map.entry(Counters.Fields.failedF24MetadataNotValid, failedByRefusalCode(SendRefusalReasonsCode.F24_METADATA_NOT_VALID)),
    Map.entry(Counters.Fields.failedTaxIdNotValid, failedByRefusalCode(SendRefusalReasonsCode.TAXID_NOT_VALID)),
    Map.entry(Counters.Fields.failedRecipientIdNotValid, failedByRefusalCode(SendRefusalReasonsCode.RECIPIENT_ID_NOT_VALID)),
    Map.entry(Counters.Fields.failedNotValidAddress, failedByRefusalCode(SendRefusalReasonsCode.NOT_VALID_ADDRESS)),
    Map.entry(Counters.Fields.failedPaymentNotValid, failedByRefusalCode(SendRefusalReasonsCode.PAYMENT_NOT_VALID)),
    Map.entry(Counters.Fields.failedServiceUnavailable, failedByRefusalCode(SendRefusalReasonsCode.SERVICE_UNAVAILABLE)),
    Map.entry(Counters.Fields.failedAddressNotFound, failedByRefusalCode(SendRefusalReasonsCode.ADDRESS_NOT_FOUND)),
    Map.entry(Counters.Fields.failedAddressSearchFailed, failedByRefusalCode(SendRefusalReasonsCode.ADDRESS_SEARCH_FAILED)),

    Map.entry(Counters.Fields.deceasedRecipient, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.RETURNED_TO_SENDER, null)
      ))
      .build()
    ),

    Map.entry(Counters.Fields.recipientNotFound, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.UNREACHABLE, null)
      ))
      .deactivatingCounters(List.of(Counters.Fields.completed))
      .build()
    ),

    Map.entry(Counters.Fields.paid, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.PU_PAID, TimelineElementCategoryV27DTO.PU_PAYMENT)
      ))
      .build()
    )
  );
}
