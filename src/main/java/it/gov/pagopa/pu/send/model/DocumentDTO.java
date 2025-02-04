package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.enums.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {
  private String fileName;
  private String digest;
  private String contentType;
  private FileStatus status;
}
