package it.gov.pagopa.pu.send.service;


import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import java.util.List;

public interface SendFacadeService {
  void preloadFiles(String sendNotificationId, String accessToken);
  void uploadFiles(String sendNotificationId);
  void deliveryNotification(String sendNotificationId, String accessToken);
  SendNotificationDTO retrieveNotificationDate(String sendNotificationId, String accessToken);
  SendNotificationDTO notificationStatus(String sendNotificationId, String accessToken);
  NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav, String accessToken);
  List<ProgressResponseElementV25DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String accessToken);
}
