package it.gov.pagopa.pu.common.pii.citizen.service;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import it.gov.pagopa.pu.common.pii.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.send.config.CacheConfig;
import it.gov.pagopa.pu.send.dto.pii.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class PersonalDataServiceTest {

  public static final Class<SendNotificationPIIDTO> CLASS_PII_DTO = SendNotificationPIIDTO.class;
  public static final PersonalDataType PERSONAL_DATA_TYPE = PersonalDataType.SEND_NOTIFICATION;

  @Mock
  private PersonalDataRepository repositoryMock;
  @Mock
  private DataCipherService cipherServiceMock;
  @Mock
  private CacheManager cacheManagerMock;

  private PersonalDataService service;

  private ConcurrentMapCache cache;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    cache = new ConcurrentMapCache(CacheConfig.Fields.pii);
    Mockito.when(cacheManagerMock.getCache(CacheConfig.Fields.pii)).thenReturn(cache);

    service = new PersonalDataService(
      repositoryMock,
      cipherServiceMock,
      cacheManagerMock
    );
  }

  @AfterEach
  void verifyNotMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      repositoryMock,
      cipherServiceMock,
      cacheManagerMock
    );
  }

  @Test
  void testInsert() {
    // Given
    SendNotificationPIIDTO pii = new SendNotificationPIIDTO();

    byte[] cipherData = new byte[0];
    Mockito.when(cipherServiceMock.encryptObj(pii)).thenReturn(cipherData);
    PersonalData personalDataInput = PersonalData.builder()
      .type(PERSONAL_DATA_TYPE.name())
      .data(cipherData)
      .build();

    long piiId = -1L;
    PersonalData personalDataOutput = PersonalData.builder()
      .id(piiId)
      .type(PERSONAL_DATA_TYPE.name())
      .data(cipherData)
      .build();

    Mockito.when(repositoryMock.save(personalDataInput)).thenReturn(personalDataOutput);

    // When
    long insert = service.insert(pii, PERSONAL_DATA_TYPE);

    // Then
    Assertions.assertEquals(piiId, insert);
    Assertions.assertSame(pii, cache.get(piiId, pii.getClass()));
  }

  //region get
  @Test
  void givenValidPersonalDataIdWhenGetThenOk() {
    // Given
    long personalDataId = 1L;
    SendNotificationPIIDTO expected = podamFactory.manufacturePojo(CLASS_PII_DTO);
    Mockito.when(repositoryMock.findById(personalDataId)).thenReturn(
      Optional.of(PersonalData.builder().id(personalDataId).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(expected);

    // When
    SendNotificationPIIDTO result = service.get(personalDataId, CLASS_PII_DTO);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, result, true, null, true));
  }

  @Test
  void givenNotFoundPersonalDataIdWhenGetThenException() {
    // Given
    long personalDataId = 1L;
    Mockito.when(repositoryMock.findById(personalDataId)).thenReturn(Optional.empty());

    // When
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.get(personalDataId, CLASS_PII_DTO));

    // Then
    Assertions.assertEquals("[PII_ENTITY_NOT_FOUND] PII Entity with id 1 not found", notFoundException.getMessage());
  }
//endregion

  //region getAll
  @Test
  void givenValidPersonalDataIdsWhenGetAllThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> personalDataIds = Set.of(pId1, pId2);
    SendNotificationPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    SendNotificationPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);

    Set<Long> cacheMissedPIds = Set.of(pId1);
    cache.put(pId2, pii2);

    Mockito.when(repositoryMock.findAllById(cacheMissedPIds)).thenReturn(List.of(
      PersonalData.builder().id(pId1).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()
    ));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(pii1);

    // When
    Map<Long, SendNotificationPIIDTO> results = service.getAll(personalDataIds, CLASS_PII_DTO);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
    Assertions.assertSame(pii2, results.get(pId2));
  }

  @Test
  void givenCompleteCachedPDataIdsWhenGetAllThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> personalDataIds = Set.of(pId1, pId2);
    SendNotificationPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    SendNotificationPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);

    cache.put(pId1, pii1);
    cache.put(pId2, pii2);

    // When
    Map<Long, SendNotificationPIIDTO> results = service.getAll(personalDataIds, CLASS_PII_DTO);

    //Then
    Assertions.assertSame(pii1, results.get(pId1));
    Assertions.assertSame(pii2, results.get(pId2));
  }

  @Test
  void givenCachedPiiWhenGetAllProtectedThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> pDataIds = Set.of(pId1, pId2);
    SendNotificationPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    SendNotificationPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    cache.put(pId2, pii2);

    List<PersonalData> pData = List.of(
      PersonalData.builder().id(pId1).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build(),
      PersonalData.builder().id(pId2).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()
    );

    Mockito.when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(pii1);

    // When
    Map<Long, SendNotificationPIIDTO> results = service.getAll(pData, pDataIds, CLASS_PII_DTO);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
    Assertions.assertSame(pii2, results.get(pId2));
  }

  @Test
  void givenNotFoundPersonalDataIdsWhenGetAllThenException() {
    // Given
    Set<Long> personalDataIds = Set.of(1L, 2L);
    Mockito.when(repositoryMock.findAllById(personalDataIds)).thenReturn(List.of(
      PersonalData.builder().id(1L).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(podamFactory.manufacturePojo(CLASS_PII_DTO));

    // When
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.getAll(personalDataIds, CLASS_PII_DTO));

    // Then
    Assertions.assertEquals("[PII_ENTITY_NOT_FOUND] PII Entities with ids 2 not found", notFoundException.getMessage());
  }

  @Test
  void whenGetAllPrivateThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> searchedPDataIds = Set.of(pId1, pId2);
    List<PersonalData> pDatas = List.of(
      PersonalData.builder()
        .id(pId1)
        .data(new byte[0])
        .type(PERSONAL_DATA_TYPE.name())
        .build(),
      PersonalData.builder()
        .id(pId2)
        .data(new byte[0])
        .type(PERSONAL_DATA_TYPE.name())
        .build(),
      PersonalData.builder()
        .id(3L)
        .data(new byte[0])
        .type(PERSONAL_DATA_TYPE.name())
        .build()
    );

    SendNotificationPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    SendNotificationPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO))
      .thenReturn(pii1)
      .thenReturn(pii2);

    // When
    Map<Long, SendNotificationPIIDTO> results = service.getAll(pDatas, searchedPDataIds, CLASS_PII_DTO);

    // Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii2, results.get(pId2), true, null, true));
  }
//endregion

  @Test
  void testDelete() {
    // Given
    long id = 1L;

    // When
    service.delete(id);

    // Then
    Mockito.verify(repositoryMock).deleteById(id);
  }
}
