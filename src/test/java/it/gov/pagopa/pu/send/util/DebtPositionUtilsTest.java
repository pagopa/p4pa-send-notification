package it.gov.pagopa.pu.send.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DebtPositionUtilsTest {

  public static final String NAV = "345670000000000199";
  public static final String SEGREGATION_CODE = "45";

  @Test
  void testExtractSegregationCodeFromNav() {
    //WHEN
    String segregationCodeFromNav = DebtPositionUtils.extractSegregationCodeFromNav(NAV);
    //THEN
    Assertions.assertEquals(SEGREGATION_CODE, segregationCodeFromNav);
  }

}
