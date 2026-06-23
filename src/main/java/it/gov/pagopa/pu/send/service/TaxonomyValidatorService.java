package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface TaxonomyValidatorService {
  void validateTaxonomyCode(Organization org, String taxonomyCode, String accessToken);
}
