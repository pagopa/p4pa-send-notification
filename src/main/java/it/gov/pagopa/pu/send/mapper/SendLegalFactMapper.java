package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactListElementV20DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactsIdV20DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactIdDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static it.gov.pagopa.pu.send.util.Constants.LEGAL_FACT_ID_PREFIX;

@Service
public class SendLegalFactMapper {

  public LegalFactListElementDTO mapLegalFactDTOFromSend(LegalFactListElementV20DTO legalFactListElementSendDto) {
    if(legalFactListElementSendDto == null) {
      return null;
    }
    return LegalFactListElementDTO.builder()
      .iun(legalFactListElementSendDto.getIun())
      .taxId(legalFactListElementSendDto.getTaxId())
      .legalFactId(this.mapLegalFactIdDTOFromSend(legalFactListElementSendDto.getLegalFactsId()))
      .build();
  }

  private LegalFactIdDTO mapLegalFactIdDTOFromSend(LegalFactsIdV20DTO legalFactIdSendDto) {
    if(legalFactIdSendDto == null) {
      return null;
    }
    return LegalFactIdDTO.builder()
        .key(this.mapLegalFactIdKey(legalFactIdSendDto.getKey()))
        .category(legalFactIdSendDto.getCategory())
        .build();
  }

  private String mapLegalFactIdKey(String legalFactIdKey) {
    return Optional.ofNullable(legalFactIdKey)
      .map(key -> key.replace(LEGAL_FACT_ID_PREFIX, ""))
      .orElse(null);
  }

}
