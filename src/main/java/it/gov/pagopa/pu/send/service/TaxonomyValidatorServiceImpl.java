package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.exception.InvalidTaxonomyException;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.model.SendTaxonomy;
import it.gov.pagopa.pu.send.repository.SendTaxonomyRepository;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaxonomyValidatorServiceImpl implements TaxonomyValidatorService {
  private final SendTaxonomyRepository sendTaxonomyRepository;

  public TaxonomyValidatorServiceImpl(SendTaxonomyRepository sendTaxonomyRepository, OrganizationService organizationService) {
    this.sendTaxonomyRepository = sendTaxonomyRepository;
  }

  @Override
  public void validateTaxonomyCode(Organization org, String taxonomyCode, String accessToken) {
    SendTaxonomy sendTaxonomy = sendTaxonomyRepository.findByTaxonomyCode(taxonomyCode);
    if(sendTaxonomy==null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_TAXONOMY_CODE_NOT_FOUND,
        String.format("Send Taxonomy having taxonomyCode %s not found", taxonomyCode));
    }

    String orgTypeCode = org.getOrgTypeCode();
    if (orgTypeCode != null && !orgTypeCode.equals(sendTaxonomy.getOrganizationType())) {
      throw new InvalidTaxonomyException(String.format("The taxonomyCode [%s] is not valid for the organization type [%s]", taxonomyCode, orgTypeCode));
    }
  }
}
