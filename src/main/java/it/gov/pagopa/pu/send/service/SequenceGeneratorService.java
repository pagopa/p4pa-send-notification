package it.gov.pagopa.pu.send.service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Objects;

import it.gov.pagopa.pu.send.model.CustomSequenceGenerator;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {
  private final MongoOperations mongoOperations;

  public SequenceGeneratorService(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  public long generateSequence(String seqName) {
    CustomSequenceGenerator counter = mongoOperations.findAndModify(query(where("_id").is(seqName)),
      new Update().inc("seq",1), options().returnNew(true).upsert(true),
      CustomSequenceGenerator.class);
    return !Objects.isNull(counter) ? counter.getSeq() : 1;
  }
}
