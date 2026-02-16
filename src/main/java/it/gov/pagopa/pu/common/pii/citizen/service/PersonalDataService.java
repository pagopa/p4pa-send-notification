package it.gov.pagopa.pu.common.pii.citizen.service;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import it.gov.pagopa.pu.common.pii.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@CacheConfig(cacheNames = it.gov.pagopa.pu.send.config.CacheConfig.Fields.pii)
public class PersonalDataService {

  private final PersonalDataRepository repository;
  private final DataCipherService dataCipherService;
  private final CacheManager cacheManager;

  public PersonalDataService(PersonalDataRepository repository, DataCipherService dataCipherService, CacheManager cacheManager) {
    this.repository = repository;
    this.dataCipherService = dataCipherService;
    this.cacheManager = cacheManager;
  }

  public long insert(Object pii, PersonalDataType type) {
    Long personalDataId = repository.save(PersonalData.builder()
      .type(type.name())
      .data(dataCipherService.encryptObj(pii))
      .build()).getId();
    Objects.requireNonNull(cacheManager.getCache(it.gov.pagopa.pu.send.config.CacheConfig.Fields.pii))
      .put(personalDataId, pii);
    return personalDataId;
  }

  @CacheEvict(key = "#personalDataId")
  public void delete(long personalDataId) {
    repository.deleteById(personalDataId);
  }

  @Cacheable(key = "#personalDataId", unless = "#result == null")
  public <T> T get(long personalDataId, Class<T> classType) {
    return repository.findById(personalDataId)
      .map(personalData -> dataCipherService.decryptObj(personalData.getData(), classType))
      .orElseThrow(() -> new NotFoundException("[PERSONAL_DATA_NOT_FOUND] installment pii not found for id " + personalDataId));
  }

}
