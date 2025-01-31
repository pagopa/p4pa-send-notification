package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendNotification;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SendNotificationRepository extends MongoRepository<SendNotification, String>, SendNotificationRepositoryExt {
  Optional<SendNotification> findBySendNotificationId(Long sendNotificationId);
}
