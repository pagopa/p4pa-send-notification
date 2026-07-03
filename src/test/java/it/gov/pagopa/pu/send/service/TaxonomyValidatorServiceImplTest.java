package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
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

  @InjectMocks
  private TaxonomyValidatorServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendTaxonomyRepositoryMock);
  }

  @Test
  void givenValidTaxonomyCodeForOrganizationWhenValidateTaxonomyCodeThenDoNothing() {
    String taxonomyCode = "010101P";
    String accessToken = "ACCESSTOKEN";

    Organization org = new Organization();
    org.setOrganizationId(1L);
    org.setOrgTypeCode("01");

    SendTaxonomy sendTaxonomy = new SendTaxonomy();
    sendTaxonomy.setTaxonomyCode(taxonomyCode);
    sendTaxonomy.setOrganizationType("01");

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(sendTaxonomy);

    assertDoesNotThrow(() -> service.validateTaxonomyCode(org, taxonomyCode, accessToken));
  }

  @Test
  void givenNotPresentTaxonomyCodeWhenValidateTaxonomyCodeThenException() {
    String taxonomyCode = "010101P";
    String accessToken = "ACCESSTOKEN";
    Organization org = new Organization();

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.validateTaxonomyCode(org, taxonomyCode, accessToken));
  }

  @Test
  void givenInvalidTaxonomyCodeForOrganizationWhenValidateTaxonomyCodeThenException() {
    String taxonomyCode = "010101P";
    String accessToken = "ACCESSTOKEN";

    Organization org = new Organization();
    org.setOrganizationId(1L);
    org.setOrgTypeCode("03");

    SendTaxonomy sendTaxonomy = new SendTaxonomy();
    sendTaxonomy.setTaxonomyCode(taxonomyCode);

    Mockito.when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode)).thenReturn(sendTaxonomy);

    assertThrows(InvalidTaxonomyException.class, () -> service.validateTaxonomyCode(org, taxonomyCode, accessToken));
  }
}
