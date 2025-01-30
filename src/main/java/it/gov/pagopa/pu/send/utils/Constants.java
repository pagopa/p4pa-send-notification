package it.gov.pagopa.pu.send.utils;

import java.time.ZoneId;

public class Constants {
    private Constants(){}

    public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");

    public enum NOTIFICATION_STATUS {
      WAITING_FILE,
      SENDING,
      REGISTERED,
      UPLOADED,
      COMPLETE;
    }
}
