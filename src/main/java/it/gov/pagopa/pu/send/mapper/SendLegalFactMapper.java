package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactDownloadMetadataResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactListElementV20DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactsIdV20DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDownloadMetadataDTO;
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
        .key(this.polishLegalFactIdKey(legalFactIdSendDto.getKey()))
        .category(legalFactIdSendDto.getCategory())
        .build();
  }

  public String polishLegalFactIdKey(String legalFactIdKey) {
    return Optional.ofNullable(legalFactIdKey)
      .map(key -> key.replace(LEGAL_FACT_ID_PREFIX, ""))
      .orElse(null);
  }

  public LegalFactDownloadMetadataDTO mapLegalFactDownloadMetadataFromSend(LegalFactDownloadMetadataResponseDTO legalFactDownloadMetadataResponseDTO) {
    if(legalFactDownloadMetadataResponseDTO == null) {
      return null;
    }
    return LegalFactDownloadMetadataDTO.builder()
      .filename(legalFactDownloadMetadataResponseDTO.getFilename())
      .contentLength(legalFactDownloadMetadataResponseDTO.getContentLength())
      .url(legalFactDownloadMetadataResponseDTO.getUrl())
      .retryAfter(legalFactDownloadMetadataResponseDTO.getRetryAfter())
      .build();
  }
}
