package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendTaxonomy;
import it.gov.pagopa.pu.send.repository.SendTaxonomyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendTaxonomyServiceImplTest {

  @Mock
  private SendTaxonomyRepository sendTaxonomyRepositoryMock;

  @InjectMocks
  private SendTaxonomyServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendTaxonomyRepositoryMock
    );
  }

  @Test
  void givenValidTaxonomyCodeWhenFindByTaxonomyCodeThenReturnSendTaxonomy() {
    //GIVEN
    String taxonomyCode = "taxonomy_code";

    SendTaxonomy expectedSendTaxonomy = new SendTaxonomy();
    expectedSendTaxonomy.setTaxonomyCode(taxonomyCode);

    when(sendTaxonomyRepositoryMock.findByTaxonomyCode(taxonomyCode))
      .thenReturn(expectedSendTaxonomy);
    //WHEN
    SendTaxonomy actualSendTaxonomy = service.findByTaxonomyCode(taxonomyCode);

    //THEN
    Assertions.assertEquals(expectedSendTaxonomy, actualSendTaxonomy);
  }

  @Test
  void givenNullTaxonomyCodeWhenFindByTaxonomyCodeThenReturnNull() {
    //WHEN
    SendTaxonomy actualSendTaxonomy = service.findByTaxonomyCode(null);

    //THEN
    Assertions.assertNull(actualSendTaxonomy);
    verify(sendTaxonomyRepositoryMock, never())
      .findByTaxonomyCode(isNull());
  }
}
