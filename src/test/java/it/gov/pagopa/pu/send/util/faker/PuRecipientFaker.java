package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.PuRecipient;

import java.util.List;

import static it.gov.pagopa.pu.send.util.faker.PuPaymentFaker.buildPuPayment;
import static it.gov.pagopa.pu.send.util.faker.RecipientFaker.buildRecipient;

public class PuRecipientFaker {

  public static PuRecipient buildPuRecipient() {
    return PuRecipient.builder()
      .recipient(buildRecipient())
      .puPayments(List.of(buildPuPayment()))
      .build();
  }
}
