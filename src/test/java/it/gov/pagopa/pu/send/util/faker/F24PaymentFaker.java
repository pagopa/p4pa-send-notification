package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.generated.F24Payment;

import static it.gov.pagopa.pu.send.util.faker.AttachmentFaker.buildAttachment;

public class F24PaymentFaker {

  public static F24Payment buildF24Payment() {
    return F24Payment.builder()
      .title("F24PAYMENT")
      .applyCost(true)
      .metadataAttachment(buildAttachment())
      .build();
  }
}
