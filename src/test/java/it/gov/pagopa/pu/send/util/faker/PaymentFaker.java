package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.generated.Payment;

import static it.gov.pagopa.pu.send.util.faker.F24PaymentFaker.buildF24Payment;
import static it.gov.pagopa.pu.send.util.faker.PagopaFaker.buildPagopa;

public class PaymentFaker {

  public static Payment buildPayment() {
    return Payment.builder()
      .pagoPa(buildPagopa())
      .f24(buildF24Payment())
      .build();
  }
}
