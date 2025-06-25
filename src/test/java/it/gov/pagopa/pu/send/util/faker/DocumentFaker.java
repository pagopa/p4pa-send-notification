package it.gov.pagopa.pu.send.util.faker;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.generated.Attachment;
import it.gov.pagopa.pu.send.dto.generated.Document;

import static it.gov.pagopa.pu.send.util.faker.AttachmentFaker.buildAttachment;

public class DocumentFaker {

  public static DocumentDTO buildDocumentDTO() {
    Attachment attachment = buildAttachment();
    return DocumentDTO.builder()
      .fileName(attachment.getFileName())
      .digest(attachment.getDigest())
      .contentType(attachment.getContentType())
      .key("docKey")
      .versionId("12345678")
      .build();
  }

  public static Document buildDocument() {
    return Document.builder()
      .fileName("document.pdf")
      .digest("sha256")
      .contentType("application/pdf")
      .build();
  }

  public static DocumentDTO buildDocumentAttachment() {
    return DocumentDTO.builder()
      .fileName("document")
      .digest("sha256")
      .contentType("application/pdf")
      .key("docKey")
      .versionId("12345678")
      .build();
  }
}
