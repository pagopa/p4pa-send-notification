package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendTaxonomy;

public interface SendTaxonomyService {
  SendTaxonomy findByTaxonomyCode(String taxonomyCode);
}
