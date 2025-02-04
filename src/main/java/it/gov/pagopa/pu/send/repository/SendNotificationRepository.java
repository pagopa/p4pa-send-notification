package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SendNotificationRepository extends MongoRepository<SendNotification, String>, SendNotificationRepositoryExt {

}
