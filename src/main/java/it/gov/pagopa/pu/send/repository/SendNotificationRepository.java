package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SendNotificationRepository extends MongoRepository<SendNotificationNoPII, String>, SendNotificationRepositoryExt {

}
