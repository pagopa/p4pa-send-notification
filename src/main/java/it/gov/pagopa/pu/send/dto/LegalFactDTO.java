package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class LegalFactDTO implements Serializable {
  private String fileName;
  private LegalFactCategoryDTO category;
  private String url;
}
