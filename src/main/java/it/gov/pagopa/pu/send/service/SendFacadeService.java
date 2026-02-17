package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDownloadMetadataDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.send.exception.NotFoundException;

import java.io.IOException;
import java.util.List;

public interface SendFacadeService {
  void preloadFiles(String sendNotificationId, String accessToken);
  void uploadFiles(String sendNotificationId);
  void deliveryNotification(String sendNotificationId, String accessToken);
  SendNotificationDTO retrieveNotificationDate(String sendNotificationId, String accessToken);
  SendNotificationDTO notificationStatus(String sendNotificationId, String accessToken);
  NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav, String accessToken);
  /**
   * Get Stream Events
   * If streamId in input is null, find last stream for this organization and take streamId
   * @param streamId stream identifier (optional)
   * @param lastEventId (optional)
   * @return List&lt;ProgressResponseElementV25DTO&gt;
   * @throws NotFoundException if doesn't exist at leat one stream
   */
  List<ProgressResponseElementV25DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String accessToken);
  SendStreamDTO getStream(String streamId, String accessToken);
  List<LegalFactListElementDTO> retrieveLegalFacts(String sendNotificationId, String accessToken);
  LegalFactDownloadMetadataDTO retrieveLegalFactDownloadMetadata(String sendNotificationId, String legalFactId, String accessToken);
  void downloadAndCacheSendLegalFact(String notificationRequestId, LegalFactCategoryDTO category, String legalFactId, String accessToken) throws IOException;
}
