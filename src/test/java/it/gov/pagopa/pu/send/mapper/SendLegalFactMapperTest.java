package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactDownloadMetadataResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactListElementV20DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactsIdV20DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDownloadMetadataDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactIdDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SendLegalFactMapperTest {

  private SendLegalFactMapper mapper;
  private LegalFactListElementV20DTO sendLegalFactDTO;
  private LegalFactDownloadMetadataResponseDTO sendLegalFactDownloadMetadataDTO;

  @BeforeEach
  void setUp() {
    mapper = new SendLegalFactMapper();

    // LegalFactId from SEND
    LegalFactsIdV20DTO legalFactsIdDTO = new LegalFactsIdV20DTO();
    legalFactsIdDTO.setKey("key");
    legalFactsIdDTO.setCategory("category");
    // LegalFact from SEND
    sendLegalFactDTO = new LegalFactListElementV20DTO();
    sendLegalFactDTO.setIun("iun");
    sendLegalFactDTO.setTaxId("ABCDEF11A01H000A");
    sendLegalFactDTO.setLegalFactsId(legalFactsIdDTO); //set id

    // LegalFactDownloadMetadata from SEND
    sendLegalFactDownloadMetadataDTO = new LegalFactDownloadMetadataResponseDTO();
    sendLegalFactDownloadMetadataDTO.setFilename("filename.pdf");
    sendLegalFactDownloadMetadataDTO.setContentLength(new BigDecimal(1234));
    sendLegalFactDownloadMetadataDTO.setUrl("http://URL");
    sendLegalFactDownloadMetadataDTO.setRetryAfter(new BigDecimal(1234));
  }

  @Test
  void givenValidSendLegalFactWhenMapThenVerify(){
    // Given
    // Mapped LegalFactId
    LegalFactIdDTO expectedLegalFactIdDTO = new LegalFactIdDTO();
    expectedLegalFactIdDTO.setKey(sendLegalFactDTO.getLegalFactsId().getKey());
    expectedLegalFactIdDTO.setCategory(sendLegalFactDTO.getLegalFactsId().getCategory());

    // Mapped LegalFact
    LegalFactListElementDTO expectedDTO  = new LegalFactListElementDTO();
    expectedDTO.setIun(sendLegalFactDTO.getIun());
    expectedDTO.setTaxId(sendLegalFactDTO.getTaxId());
    expectedDTO.setLegalFactId(expectedLegalFactIdDTO); //set id

    // When
    LegalFactListElementDTO resultDTO = mapper.mapLegalFactDTOFromSend(sendLegalFactDTO);

    // Then
    assertNotNull(resultDTO);
    TestUtils.checkNotNullFields(resultDTO);
    assertEquals(expectedDTO, resultDTO);
  }

  @Test
  void givenValidSendLegalFactWhenMapThenMapToNull(){
    // Given

    // When
    LegalFactListElementDTO resultDTO = mapper.mapLegalFactDTOFromSend(null);

    // Then
    assertNull(resultDTO);
  }

  @Test
  void givenNullLegalFactIdWhenMapThenVerify() {
    // Given
    sendLegalFactDTO.setLegalFactsId(null);

    // Mapped LegalFact
    LegalFactListElementDTO expectedDTO  = new LegalFactListElementDTO();
    expectedDTO.setIun(sendLegalFactDTO.getIun());
    expectedDTO.setTaxId(sendLegalFactDTO.getTaxId());
    expectedDTO.setLegalFactId(null); //set id

    // When
    LegalFactListElementDTO resultDTO = mapper.mapLegalFactDTOFromSend(sendLegalFactDTO);

    // Then
    assertNotNull(resultDTO);
    TestUtils.checkNotNullFields(resultDTO, "legalFactId");
    assertNull(resultDTO.getLegalFactId());
    assertEquals(expectedDTO, resultDTO);
  }

  @Test
  void givenValidSendLegalFactDownloadMetadataWhenMapThenVerify(){
    // Given
    // Mapped LegalFactDownloadMetadata
    LegalFactDownloadMetadataDTO expectedDTO  = new LegalFactDownloadMetadataDTO();
    expectedDTO.setFilename(sendLegalFactDownloadMetadataDTO.getFilename());
    expectedDTO.setContentLength(sendLegalFactDownloadMetadataDTO.getContentLength());
    expectedDTO.setUrl(sendLegalFactDownloadMetadataDTO.getUrl());
    expectedDTO.setRetryAfter(sendLegalFactDownloadMetadataDTO.getRetryAfter());

    // When
    LegalFactDownloadMetadataDTO actualDTO = mapper.mapLegalFactDownloadMetadataFromSend(sendLegalFactDownloadMetadataDTO);

    // Then
    assertNotNull(actualDTO);
    TestUtils.checkNotNullFields(actualDTO);
    assertEquals(expectedDTO, actualDTO);
  }

  @Test
  void givenValidSendLegalFactDownloadMetadataWhenMapThenMapToNull(){
    // Given

    // When
    LegalFactDownloadMetadataDTO actualDTO = mapper.mapLegalFactDownloadMetadataFromSend(null);

    // Then
    assertNull(actualDTO);
  }
}
