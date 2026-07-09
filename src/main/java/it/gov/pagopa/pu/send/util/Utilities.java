package it.gov.pagopa.pu.send.util;

import org.slf4j.MDC;

import java.util.Optional;

public class Utilities {
    private Utilities(){}

    public static String getTraceId(){
        return MDC.get("traceId");
    }

  public static <E extends Enum<E>> Optional<E> safeEnumFromValue(Class<E> c, String value) {
    try {
      return Optional.of(E.valueOf(c, value));
    } catch (IllegalArgumentException | NullPointerException e) {
      return Optional.empty();
    }
  }

}
