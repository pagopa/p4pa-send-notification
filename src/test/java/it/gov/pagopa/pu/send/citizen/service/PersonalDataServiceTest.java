package it.gov.pagopa.pu.send.citizen.service;

import it.gov.pagopa.pu.send.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.send.citizen.model.PersonalData;
import it.gov.pagopa.pu.send.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.send.config.CacheConfig;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import java.util.Optional;
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


@ExtendWith(MockitoExtension.class)
class PersonalDataServiceTest {

  @Mock
  private PersonalDataRepository repositoryMock;
  @Mock
  private DataCipherService cipherServiceMock;
  @Mock
  private CacheManager cacheManagerMock;

  private PersonalDataService service;

  @BeforeEach
  void init() {
    service = new PersonalDataService(repositoryMock, cipherServiceMock, cacheManagerMock);
  }

  @AfterEach
  void verifyNotMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      repositoryMock,
      cipherServiceMock);
  }

  @Test
  void testInsert() {
    // Given
    SendNotificationPIIDTO pii = new SendNotificationPIIDTO();

    byte[] cipherData = new byte[0];
    Mockito.when(cipherServiceMock.encryptObj(pii)).thenReturn(cipherData);
    PersonalData personalDataInput = PersonalData.builder()
      .type("SEND_NOTIFICATION")
      .data(cipherData)
      .build();

    long piiId = -1L;
    PersonalData personalDataOutput = PersonalData.builder()
      .id(piiId)
      .type("SEND_NOTIFICATION")
      .data(cipherData)
      .build();

    Mockito.when(repositoryMock.save(personalDataInput)).thenReturn(personalDataOutput);
    ConcurrentMapCache cache = new ConcurrentMapCache(CacheConfig.Fields.pii);
    Mockito.when(cacheManagerMock.getCache(CacheConfig.Fields.pii)).thenReturn(cache);

    // When
    long insert = service.insert(pii, PersonalDataType.SEND_NOTIFICATION);

    // Then
    Assertions.assertEquals(piiId, insert);
    Assertions.assertSame(pii, cache.get(piiId, pii.getClass()));
  }

  //region get

  @Test
  void givenValidPersonalDataIdWhenGetThenOk(){
    SendNotificationPIIDTO expected = new SendNotificationPIIDTO();
    //given
    Mockito.when(repositoryMock.findById(1L)).thenReturn(
      Optional.of(PersonalData.builder().id(1L).data(new byte[0]).type("SEND_NOTIFICATION").build()));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], SendNotificationPIIDTO.class)).thenReturn(expected);
    //when
    SendNotificationPIIDTO sendNotificationPIIDTO = service.get(1L, SendNotificationPIIDTO.class);
    //then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, sendNotificationPIIDTO,true, null, true));
    Mockito.verify(repositoryMock, Mockito.times(1)).findById(1L);
    Mockito.verify(cipherServiceMock, Mockito.times(1)).decryptObj(new byte[0], SendNotificationPIIDTO.class);
  }

  @Test
  void givenNotFoundPersonalDataIdWhenGetThenException(){
    //given
    Mockito.when(repositoryMock.findById(1L)).thenReturn(Optional.empty());
    //when
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.get(1L, SendNotificationPIIDTO.class));
    //then
    Assertions.assertEquals("[PERSONAL_DATA_NOT_FOUND] installment pii not found for id 1", notFoundException.getMessage());
    Mockito.verify(repositoryMock, Mockito.times(1)).findById(1L);
    Mockito.verifyNoInteractions(cipherServiceMock);
  }
  //endregion

  @Test
  void testDelete(){
    // Given
    long id = 1L;

    // When
    service.delete(id);

    // Then
    Mockito.verify(repositoryMock).deleteById(id);
  }
}
