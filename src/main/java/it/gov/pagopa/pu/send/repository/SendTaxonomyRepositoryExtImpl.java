package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendTaxonomy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


public class SendTaxonomyRepositoryExtImpl implements SendTaxonomyRepositoryExt {
  private final MongoTemplate mongoTemplate;

  public SendTaxonomyRepositoryExtImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public SendTaxonomy findByTaxonomyCode(String taxonomyCode) {
    Query query = Query.query(Criteria.where(SendTaxonomy.Fields.taxonomyCode)
      .is(taxonomyCode));
    return mongoTemplate.findOne(query, SendTaxonomy.class);
  }
}
