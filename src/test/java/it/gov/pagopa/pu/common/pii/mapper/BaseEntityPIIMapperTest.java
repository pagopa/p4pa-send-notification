package it.gov.pagopa.pu.common.pii.mapper;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.common.pii.dto.TestFullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.TestPIIDTO;
import it.gov.pagopa.pu.send.model.TestNoPIIEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

@ExtendWith(MockitoExtension.class)
class BaseEntityPIIMapperTest {
  private TestPIIMapper mapper;
  private TestFullEntityPIIDTO fullDTO;

  @BeforeEach
  void setUp() {
    mapper = new TestPIIMapper();
    fullDTO = new TestFullEntityPIIDTO("Mario Rossi", "test@example.com", 1L);
  }

  @Test
  void givenValidFullDTOWhenMapThenReturnPairEntity(){
    // Given
    TestNoPIIEntity expectedNoPii = new TestNoPIIEntity(1L);
    TestPIIDTO expectedPii = new TestPIIDTO("Mario Rossi", "test@example.com");

    // When
    Pair<TestNoPIIEntity, TestPIIDTO> result = mapper.map(fullDTO);

    // Then
    assertNotNull(result);
    assertNotNull(result.getFirst());
    assertNotNull(result.getSecond());
    assertEquals(expectedNoPii.getPersonalDataId(), result.getFirst().getPersonalDataId());
    assertEquals(expectedPii.getFullName(), result.getSecond().getFullName());
    assertEquals(expectedPii.getEmail(), result.getSecond().getEmail());
  }

  @Test
  void givenValidFullDTOWithNullNoPIIWhenMapThenReturnPairEntityWithoutPersonalDataId() {
    // Given
    fullDTO.setNoPII(null);

    // When
    Pair<TestNoPIIEntity, TestPIIDTO> result = mapper.map(fullDTO);

    // Then
    assertNotNull(result);
    assertNull(result.getFirst().getPersonalDataId());
  }

  @Test
  void givenFullDTOWithValidNoPIIWhenMapThenVerifyPersonalDataId() {
    // Given
    TestNoPIIEntity noPii = new TestNoPIIEntity(1L);
    noPii.setPersonalDataId(123L);
    fullDTO.setNoPII(noPii);

    // When
    Pair<TestNoPIIEntity, TestPIIDTO> result = mapper.map(fullDTO);

    // Then
    assertNotNull(result);
    assertEquals(123L, result.getFirst().getPersonalDataId());
  }


  @Test
  void givenFullDTOWhenExtractNoPiiEntityThenVerify() {
    // When
    TestNoPIIEntity result = mapper.extractNoPiiEntity(fullDTO);

    // Then
    assertNotNull(result);
    assertEquals(fullDTO.getId(), result.getId());
  }

  @Test
  void givenFullDTOWhenExtractPiiEntityThenVerify() {
    // When
    TestPIIDTO result = mapper.extractPiiDto(fullDTO);

    // Then
    assertNotNull(result);
    assertEquals(fullDTO.getFullName(), result.getFullName());
    assertEquals(fullDTO.getEmail(), result.getEmail());
  }

  // Test mapper implementation
  private static class TestPIIMapper extends BaseEntityPIIMapper<TestFullEntityPIIDTO, TestNoPIIEntity, TestPIIDTO> {
    @Override
    protected TestNoPIIEntity extractNoPiiEntity(TestFullEntityPIIDTO fullDTO) {
      return new TestNoPIIEntity(fullDTO.getId());
    }

    @Override
    protected TestPIIDTO extractPiiDto(TestFullEntityPIIDTO fullDTO) {
      return new TestPIIDTO(fullDTO.getFullName(), fullDTO.getEmail());
    }

    @Override
    public TestFullEntityPIIDTO map(TestNoPIIEntity noPii) {
      return null;
    }
  }


}
