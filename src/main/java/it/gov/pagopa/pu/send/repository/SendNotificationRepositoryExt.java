package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;

public interface SendNotificationRepositoryExt {
  UpdateResult updateFilePreloadInformation(String sendNotificationId, PreLoadResponseDTO preLoad);
  UpdateResult updateNotificationStatus(String sendNotificationId, NotificationStatus newStatus);
  UpdateResult updateNotificationRequestId(String sendNotificationId, String notificationRequestId);
  UpdateResult updateFileStatus(String sendNotificationId, String fileName, FileStatus newStatus);
  UpdateResult updateFileVersionId(String sendNotificationId, String fileName, String versionId);
}
