package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.generated.PagoPa;

import static it.gov.pagopa.pu.send.util.faker.AttachmentFaker.buildAttachment;

public class PagopaFaker {

  public static PagoPa buildPagopa() {
    return PagoPa.builder()
      .creditorTaxId("CREDITORTAXID")
      .noticeCode("NOTICECODE")
      .applyCost(true)
      .attachment(buildAttachment())
      .build();
  }
}
