package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.enums.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class DocumentDTO {
  private String fileName;
  private String digest;
  private String contentType;
  private FileStatus status;
  //Send domain
  private String secret;
  private String httpMethod;
  private String url;
  private String key;
}
