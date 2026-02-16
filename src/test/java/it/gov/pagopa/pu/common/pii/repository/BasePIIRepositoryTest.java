package it.gov.pagopa.pu.common.pii.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.TestFullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.TestPIIDTO;
import it.gov.pagopa.pu.common.pii.mapper.BasePIIMapper;
import it.gov.pagopa.pu.send.model.TestNoPIIEntity;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.util.Pair;

@ExtendWith(MockitoExtension.class)
class BasePIIRepositoryTest {

  @Mock
  private BasePIIMapper<TestFullEntityPIIDTO, TestNoPIIEntity, TestPIIDTO> mockPiiMapper;

  @Mock
  private PersonalDataService mockPersonalDataService;

  @Mock
  private MongoRepository<TestNoPIIEntity, Long> mockNoPIIRepository;

  private BasePIIRepository<TestFullEntityPIIDTO, TestNoPIIEntity, TestPIIDTO, Long> repository;

  @BeforeEach
  void setUp() {
    repository = new TestPIIRepository(mockPiiMapper, mockPersonalDataService, mockNoPIIRepository);
  }

  @Test
  void givenNewEntityWhenSaveThenSuccess() {
    // Given
    TestFullEntityPIIDTO fullDTO = new TestFullEntityPIIDTO("Mario Rossi", "test@email.com", null);
    TestNoPIIEntity noPii = new TestNoPIIEntity(null);
    TestPIIDTO pii = new TestPIIDTO("Mario Rossi", "test@email.com");

    Mockito.when(mockPiiMapper.map(fullDTO)).thenReturn(Pair.of(noPii, pii));
    Mockito.when(mockPersonalDataService.insert(any(), any())).thenReturn(123L);
    Mockito.when(mockNoPIIRepository.save(any())).thenReturn(noPii);

    // When
    TestFullEntityPIIDTO result = repository.save(fullDTO);

    // Then
    assertNotNull(result);
    Mockito.verify(mockPersonalDataService).insert(pii, PersonalDataType.SEND_NOTIFICATION);
    Mockito.verify(mockNoPIIRepository).save(noPii);
  }

  @Test
  void givenExistingEntityWithoutPIIChangeWhenSaveThenSuccess() {
    // Given
    TestFullEntityPIIDTO fullDTO = new TestFullEntityPIIDTO( "Mario Rossi", "test@email.com", 1L);
    TestNoPIIEntity noPii = new TestNoPIIEntity(1L);
    noPii.setPersonalDataId(123L);
    TestPIIDTO pii = new TestPIIDTO("Mario Rossi","test@email.com");

    Mockito.when(mockPiiMapper.map(fullDTO)).thenReturn(Pair.of(noPii, pii));
    Mockito.when(mockPersonalDataService.get(123L, TestPIIDTO.class)).thenReturn(pii);
    Mockito.when(mockNoPIIRepository.save(any())).thenReturn(noPii);

    // When
    TestFullEntityPIIDTO result = repository.save(fullDTO);

    // Then
    assertNotNull(result);
    Mockito.verify(mockPersonalDataService, never()).insert(any(), any());
  }

  @Test
  void givenExistingEntityWithPIIChangeWhenSaveThenSuccess() {
    // Given
    TestFullEntityPIIDTO fullDTO = new TestFullEntityPIIDTO("Mario Rossi", "test@email.com", 1L);
    TestNoPIIEntity noPii = new TestNoPIIEntity(1L);
    noPii.setPersonalDataId(123L);
    TestPIIDTO newPii = new TestPIIDTO("Mario Bianchi", "test@email.com");
    TestPIIDTO oldPii = new TestPIIDTO("Mario Rossi", "test@email.com");

    Mockito.when(mockPiiMapper.map(fullDTO)).thenReturn(Pair.of(noPii, newPii));
    Mockito.when(mockPersonalDataService.get(123L, TestPIIDTO.class)).thenReturn(oldPii);
    Mockito.when(mockPersonalDataService.insert(any(), any())).thenReturn(456L);
    Mockito.when(mockNoPIIRepository.save(any())).thenReturn(noPii);

    // When
    TestFullEntityPIIDTO result = repository.save(fullDTO);

    // Then
    assertNotNull(result);
    Mockito.verify(mockPersonalDataService).delete(123L);
    Mockito.verify(mockPersonalDataService).insert(newPii, PersonalDataType.SEND_NOTIFICATION);
  }

  @Test
  void givenValidPersonalDataIdWhenRetrievePIIThenReturnsData() {
    // Given
    TestNoPIIEntity noPii = new TestNoPIIEntity(1L);
    noPii.setPersonalDataId(123L);
    TestPIIDTO pii = new TestPIIDTO("Mario Rossi", "test@email.com");

    Mockito.when(mockPersonalDataService.get(123L, TestPIIDTO.class)).thenReturn(pii);

    // When
    Pair<Long, Optional<TestPIIDTO>> result = repository.retrievePII(noPii);

    // Then
    assertNotNull(result);
    assertEquals(123L, result.getFirst());
    assertTrue(result.getSecond().isPresent());
    assertEquals(pii, result.getSecond().get());
  }

  @Test
  void givenNoPersonalDataIdWhenRetrievePIIThenReturnsNull() {
    // Given
    TestNoPIIEntity noPii = new TestNoPIIEntity(1L);

    // When
    Pair<Long, Optional<TestPIIDTO>> result = repository.retrievePII(noPii);

    // Then
    assertNull(result);
  }
}
