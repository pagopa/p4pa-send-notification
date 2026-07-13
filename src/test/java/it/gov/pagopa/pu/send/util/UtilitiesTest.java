package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Optional;

public class UtilitiesTest {

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }

  @Test
  void givenCorrectEnumWhenSafeEnumFromValueThenOk() {
    //GIVEN
    TimelineElementCategoryV27DTO expectedEnum = TimelineElementCategoryV27DTO.REQUEST_ACCEPTED;
    String enumStringValue = expectedEnum.getValue();

    //WHEN
    Optional<TimelineElementCategoryV27DTO> actualEnum =
      Utilities.safeEnumFromValue(TimelineElementCategoryV27DTO.class, enumStringValue);

    //THEN
    Assertions.assertTrue(actualEnum.isPresent());
    Assertions.assertEquals(expectedEnum, actualEnum.get());
  }

  @Test
  void givenIncorrectEnumWhenSafeEnumFromValueThenReturnEmpty() {
    //GIVEN
    String enumStringValue = "WRONG_ENUM_VALUE";

    //WHEN
    Optional<TimelineElementCategoryV27DTO> actualEnum =
      Utilities.safeEnumFromValue(TimelineElementCategoryV27DTO.class, enumStringValue);

    //THEN
    Assertions.assertTrue(actualEnum.isEmpty());
  }

  @Test
  void givenNullEnumWhenSafeEnumFromValueThenReturnEmpty() {
    //GIVEN
    String enumStringValue = null;

    //WHEN
    Optional<TimelineElementCategoryV27DTO> actualEnum =
      Utilities.safeEnumFromValue(TimelineElementCategoryV27DTO.class, enumStringValue);

    //THEN
    Assertions.assertTrue(actualEnum.isEmpty());
  }

}
