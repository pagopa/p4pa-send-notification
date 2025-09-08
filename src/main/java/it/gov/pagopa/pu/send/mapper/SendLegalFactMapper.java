package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactListElementV20DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactsIdV20DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactIdDTO;
import org.springframework.stereotype.Service;

@Service
public class SendLegalFactMapper {

  public LegalFactListElementDTO mapLegalFactDTOFromSend(LegalFactListElementV20DTO legalFactListElementSendDto) {
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
        .key(legalFactIdSendDto.getKey())
        .category(legalFactIdSendDto.getCategory())
        .build();
  }
}
