package it.gov.pagopa.pu.send.service;

public interface TaxonomyValidatorService {
  void validateTaxonomyCode(Long organizationId, String taxonomyCode, String accessToken);
}
