package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionOrigin;

import java.time.ZoneId;
import java.util.List;

public class Constants {
    private Constants(){}

    public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");

  public static final List<DebtPositionOrigin> ORDINARY_DEBT_POSITION_ORIGINS = List.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.RECEIPT_FILE
  );
}
