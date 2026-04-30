package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.generated.Attachment;

public class AttachmentFaker {

  public static Attachment buildAttachment() {
    return Attachment.builder()
      .contentType("application/pdf")
      .digest("sha256")
      .fileName("attachment")
      .build();
  }
}
