package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendTaxonomy;
import it.gov.pagopa.pu.send.repository.SendTaxonomyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = it.gov.pagopa.pu.send.config.CacheConfig.Fields.taxonomy)
public class SendTaxonomyServiceImpl implements SendTaxonomyService {

  private final SendTaxonomyRepository sendTaxonomyRepository;

  @Override
  @Cacheable(key = "'taxonomyCode-' + #taxonomyCode", unless = "#result == null")
  public SendTaxonomy findByTaxonomyCode(String taxonomyCode) {
    if(taxonomyCode==null) {
      return null;
    }
    return sendTaxonomyRepository.findByTaxonomyCode(taxonomyCode);
  }

}
