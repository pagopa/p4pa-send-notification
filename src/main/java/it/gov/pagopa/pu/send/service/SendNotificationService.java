package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;

import java.io.InputStream;
import java.util.List;

public interface SendNotificationService {

  CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest, String accessToken);
  StartNotificationResponse startSendNotification(String sendNotificationId, LoadFileRequest loadFileRequest, String accessToken);
  void deleteSendNotification(String sendNotificationId);
  SendNotificationDTO findSendNotificationDTO(String sendNotificationId);
  SendNotificationDTO findSendNotificationDTOByNotificationRequestId(String notificationRequestId);
  SendNotificationDTO findSendNotificationByOrgIdAndNav(Long organizationId, String nav);
  UpdateResult updateNotificationStatus(String notificationRequestId, NotificationStatus newStatus);
  void uploadSendLegalFact(String sendNotificationId, LegalFactCategoryDTO category, String fileName, InputStream inputStream);
  List<LegalFactDTO> getLegalFacts(String sendNotificationId);
}
