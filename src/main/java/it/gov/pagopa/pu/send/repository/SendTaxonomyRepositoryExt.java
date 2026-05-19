package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendTaxonomy;

public interface SendTaxonomyRepositoryExt {
  SendTaxonomy findByTaxonomyCode(String taxonomyCode);
}
