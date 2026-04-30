package it.gov.pagopa.pu.send.util;

public class DebtPositionUtils {

  private DebtPositionUtils(){}

  public static String extractSegregationCodeFromNav(String nav) {
    return nav.substring(1,3); //ex. nav: "345670000000000199" -> segregationCode: "45"
  }

}
