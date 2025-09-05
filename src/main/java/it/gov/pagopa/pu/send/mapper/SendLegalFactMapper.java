package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactListElementV20DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactsIdV20DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactIdDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SendLegalFactMapper {

  private final DataCipherService dataCipherService;

  public SendLegalFactMapper(DataCipherService dataCipherService) {
    this.dataCipherService = dataCipherService;
  }

  public LegalFactListElementDTO mapLegalFactDTOFromSend(LegalFactListElementV20DTO legalFactListElementSendDto) {
    return LegalFactListElementDTO.builder()
      .iun(legalFactListElementSendDto.getIun())
      .taxId(
        Optional.ofNullable(legalFactListElementSendDto.getTaxId())
          .map(dataCipherService::hash)
          .orElse(null)
      ).legalFactId(this.mapLegalFactIdDTOFromSend(legalFactListElementSendDto.getLegalFactsId()))
      .build();
  }

  private LegalFactIdDTO mapLegalFactIdDTOFromSend(LegalFactsIdV20DTO legalFactIdSendDto) {
    return LegalFactIdDTO.builder()
        .key(legalFactIdSendDto.getKey())
        .category(legalFactIdSendDto.getCategory())
        .build();
  }
}
