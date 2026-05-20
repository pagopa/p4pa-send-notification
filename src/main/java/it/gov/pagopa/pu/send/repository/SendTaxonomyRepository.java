package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendTaxonomy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface SendTaxonomyRepository extends MongoRepository<SendTaxonomy, String> {
  SendTaxonomy findByTaxonomyCode(String taxonomyCode);
}
