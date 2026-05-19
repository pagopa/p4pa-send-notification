package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.exception.InvalidTaxonomyException;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.model.SendTaxonomy;
import it.gov.pagopa.pu.send.repository.SendTaxonomyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyValidatorServiceImplTest {
  @Mock
  private SendTaxonomyRepository sendTaxonomyRepositoryMock;
  @Mock
  private OrganizationService organizationServiceMock;

  @InjectMocks
  private TaxonomyValidatorServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendTaxonomyRepositoryMock, organizationServiceMock);
  }

  @Test
  void givenValidTaxonomyCodeForOrganizationWhenValidateTaxonomyCodeThenDoNothing() {
    String taxonomyCode = "010101P";
    Long orgId = 1L;
    String accessToken = "ACCESSTOKEN";

    SendTaxonomy sendTaxonomy = new SendTaxonomy();
    sendTaxonomy.setTaxonomyCode(taxonomyCode);
    sendTaxonomy.setOrganizationType("01");

    Organization organization = new Organization();
    organization.setOrgTypeCode("01");

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(sendTaxonomy);
    Mockito.when(organizationServiceMock.getOrganization(orgId, accessToken)).thenReturn(organization);

    assertDoesNotThrow(() -> service.validateTaxonomyCode(orgId, taxonomyCode, accessToken));
  }

  @Test
  void givenNotPresentTaxonomyCodeWhenValidateTaxonomyCodeThenException() {
    String taxonomyCode = "010101P";
    Long orgId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.validateTaxonomyCode(orgId, taxonomyCode, accessToken));
  }

  @Test
  void givenNotPresentOrganizationWhenValidateTaxonomyCodeThenException() {
    String taxonomyCode = "010101P";
    Long orgId = 1L;
    String accessToken = "ACCESSTOKEN";

    SendTaxonomy sendTaxonomy = new SendTaxonomy();

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(sendTaxonomy);
    Mockito.when(organizationServiceMock.getOrganization(orgId, accessToken)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.validateTaxonomyCode(orgId, taxonomyCode, accessToken));
  }

  @Test
  void givenInvalidTaxonomyCodeForOrganizationWhenValidateTaxonomyCodeThenException() {
    String taxonomyCode = "010101P";
    Long orgId = 1L;
    String accessToken = "ACCESSTOKEN";

    SendTaxonomy sendTaxonomy = new SendTaxonomy();
    sendTaxonomy.setTaxonomyCode(taxonomyCode);

    Organization organization = new Organization();
    organization.setOrgTypeCode("03");

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(sendTaxonomy);
    Mockito.when(organizationServiceMock.getOrganization(orgId, accessToken)).thenReturn(organization);

    assertThrows(InvalidTaxonomyException.class, () -> service.validateTaxonomyCode(orgId, taxonomyCode, accessToken));
  }
}
