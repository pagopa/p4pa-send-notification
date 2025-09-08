package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactListElementV20DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactsIdV20DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactIdDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SendLegalFactMapperTest {

  private SendLegalFactMapper mapper;
  private LegalFactListElementV20DTO sendLegalFactDTO;

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
    assertNotNull(resultDTO.getIun());
    assertNotNull(resultDTO.getTaxId());
    assertNotNull(resultDTO.getLegalFactId());
    assertEquals(expectedDTO.getIun(), resultDTO.getIun());
    assertEquals(expectedDTO.getTaxId(), resultDTO.getTaxId());
    assertEquals(expectedDTO.getLegalFactId(), resultDTO.getLegalFactId());
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
    assertNotNull(resultDTO.getIun());
    assertNotNull(resultDTO.getTaxId());
    assertNull(resultDTO.getLegalFactId());
    assertEquals(expectedDTO.getIun(), resultDTO.getIun());
    assertEquals(expectedDTO.getTaxId(), resultDTO.getTaxId());
  }

}
