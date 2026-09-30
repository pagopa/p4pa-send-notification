package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendTaxonomy;
import it.gov.pagopa.pu.send.repository.SendTaxonomyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendTaxonomyServiceImpl implements SendTaxonomyService {

  private final SendTaxonomyRepository sendTaxonomyRepository;

  @Override
  public SendTaxonomy findByTaxonomyCode(String taxonomyCode) {
    return sendTaxonomyRepository.findByTaxonomyCode(taxonomyCode);
  }

}
