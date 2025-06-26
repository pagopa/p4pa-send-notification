package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.generated.Recipient;

import java.util.List;

import static it.gov.pagopa.pu.send.util.faker.AddressFaker.buildDigitalAddress;
import static it.gov.pagopa.pu.send.util.faker.AddressFaker.buildPhysicalAddress;
import static it.gov.pagopa.pu.send.util.faker.PaymentFaker.buildPayment;

public class RecipientFaker {

  public static Recipient buildRecipient() {
    return Recipient.builder()
      .recipientType(Recipient.RecipientTypeEnum.fromValue("PF"))
      .taxId("BNRMHL75C06G702B")
      .denomination("ROSSI MARIO")
      .physicalAddress(buildPhysicalAddress())
      .digitalDomicile(buildDigitalAddress())
      .payments(List.of(buildPayment()))
      .build();
  }
}
