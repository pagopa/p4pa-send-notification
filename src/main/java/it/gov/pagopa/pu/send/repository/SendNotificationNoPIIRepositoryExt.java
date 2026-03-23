package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface SendNotificationNoPIIRepositoryExt {
  UpdateResult updateFilePreloadInformation(String sendNotificationId, PreLoadResponseDTO preLoad);
  UpdateResult updateNotificationStatus(String notificationRequestId, NotificationStatus newStatus);
  UpdateResult updateNotificationRequestId(String sendNotificationId, String notificationRequestId);
  UpdateResult updateFileStatus(String sendNotificationId, String fileName, FileStatus newStatus);
  UpdateResult updateFileVersionId(String sendNotificationId, String fileName, String versionId);
  UpdateResult updateNotificationIun(String sendNotificationId, String iun);
  UpdateResult updateNotificationDate(String sendNotificationId, OffsetDateTime notificationDate, String nav);
  Optional<SendNotificationNoPII> findByIdAndOrganizationId(String notificationId, Long organizationId);
  Optional<SendNotificationNoPII> findByOrganizationIdAndNav(Long organizationId, String nav);
  Optional<SendNotificationNoPII> findByNotificationRequestId(String notificationRequestId);
  UpdateResult addLegalFact(String sendNotificationId, LegalFactDTO legalFact);
}
