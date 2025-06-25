package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.PuPayment;

import static it.gov.pagopa.pu.send.util.faker.PaymentFaker.buildPayment;

public class PuPaymentFaker {

  public static PuPayment buildPuPayment() {
    return PuPayment.builder()
      .debtPositionId(1L)
      .payment(buildPayment())
      .build();
  }
}
