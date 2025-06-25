package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.generated.Address;
import it.gov.pagopa.pu.send.dto.generated.NotificationDigitalAddress;

public class AddressFaker {

  public static Address buildPhysicalAddress() {
    return Address.builder()
      .address("Via Larga 10")
      .zip("00186")
      .municipality("Roma")
      .province("RM")
      .build();
  }

  public static NotificationDigitalAddress buildDigitalAddress() {
    return NotificationDigitalAddress.builder()
      .address("account@domain.it")
      .type(NotificationDigitalAddress.TypeEnum.PEC)
      .build();
  }
}
