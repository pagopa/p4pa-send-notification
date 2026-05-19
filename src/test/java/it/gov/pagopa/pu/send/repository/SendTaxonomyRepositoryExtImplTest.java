package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendTaxonomy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


@ExtendWith(MockitoExtension.class)
class SendTaxonomyRepositoryExtImplTest extends BaseMongoRepositoryTest{

  @InjectMocks
  private SendTaxonomyRepositoryExtImpl repository;

  @Test
  void givenTaxonomyCodeWhenFindByTaxonomyCodeThenVerify() {
    String taxonomyCode = "010101P";

    SendTaxonomy expectedResponse = new SendTaxonomy();
    expectedResponse.setTaxonomyCode(taxonomyCode);

    Query query = Query.query(Criteria.where(SendTaxonomy.Fields.taxonomyCode)
      .is(taxonomyCode));

    Mockito.when(mongoTemplateMock.findOne(query, SendTaxonomy.class))
      .thenReturn(expectedResponse);

    SendTaxonomy result = repository.findByTaxonomyCode(taxonomyCode);

    Assertions.assertEquals(expectedResponse, result);
  }
}
