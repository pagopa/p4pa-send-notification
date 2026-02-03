package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.model.SendStream.Fields;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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

  @Override
  public UpdateResult updateLastEventId(String streamId, String lastEventId) {
    Query query = new Query();
    query.addCriteria(
      Criteria.where(Fields.streamId).is(streamId)
    );
    Update update = new Update().push(Fields.lastEventId, lastEventId);
    return mongoTemplate.updateFirst(query, update, SendStream.class);
  }
}
