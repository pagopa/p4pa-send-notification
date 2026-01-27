package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendStream;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class SendStreamRepositoryExtImpl implements SendStreamRepositoryExt {

  private final MongoTemplate mongoTemplate;

  public SendStreamRepositoryExtImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<SendStream> findByIpaCode(String organizationIpaCode) {
    Query query = new Query();
    query.addCriteria(
      Criteria.where(SendStream.Fields.organizationIpaCode).is(organizationIpaCode)
    );
    return mongoTemplate.find(query, SendStream.class);
  }
}
