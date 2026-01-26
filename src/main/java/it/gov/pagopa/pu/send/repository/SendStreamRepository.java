package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendStream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface SendStreamRepository extends MongoRepository<SendStream, String> {
}
