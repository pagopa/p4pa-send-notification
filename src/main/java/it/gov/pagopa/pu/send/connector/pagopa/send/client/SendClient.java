package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;

  private final ConcurrentHashMap<String, StreamMetadataResponseV25DTO> streamCache = new ConcurrentHashMap<>();


  public SendClient(PagopaSendApisHolder apisHolder) {
    this.apisHolder = apisHolder;
  }

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, String apiKey, String pdndAccessToken) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey, pdndAccessToken)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, String apiKey, String pdndAccessToken) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey, pdndAccessToken)
      .sendNewNotificationV24(newNotificationRequestV24DTO);
  }

  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, String apiKey, String pdndAccessToken) {
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey, pdndAccessToken)
      .retrieveNotificationRequestStatusV24(notificationRequestId, null, null);
  }

  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, String apiKey, String pdndAccessToken) {
    return apisHolder.getNotificationPriceApi(apiKey, pdndAccessToken).retrieveNotificationPriceV23(paTaxId, noticeCode);
  }

  public StreamMetadataResponseV25DTO createStream(StreamCreationRequestV25DTO createStreamRequest, String apikey, String pdndAccessToken){
    return streamCache.computeIfAbsent(apikey, key ->
      apisHolder.getStreamsApi(apikey, pdndAccessToken).createEventStreamV25(createStreamRequest));
  }

  public List<StreamListElementDTO> getStreams(String apikey, String pdndAccessToken){
    return apisHolder.getStreamsApi(apikey, pdndAccessToken).listEventStreamsV25();
  }

  public List<ProgressResponseElementV25DTO> getStreamEvents(String streamId, String lastEventId, String apiKey, String pdndAccessToken){
    return apisHolder.getEventsApi(apiKey, pdndAccessToken)
      .consumeEventStreamV25(UUID.fromString(streamId), lastEventId);
  }

  public List<LegalFactListElementV20DTO> getLegalFacts(String iun, String apiKey, String pdndAccessToken){
    return apisHolder.getLegalFactsApiByApiKey(apiKey, pdndAccessToken)
      .retrieveNotificationLegalFactsV20(iun);
  }

  public LegalFactDownloadMetadataResponseDTO getLegalFactDownloadMetadata(String iun, String legalFactId, String apiKey, String pdndAccessToken){
    return apisHolder.getLegalFactsApiByApiKey(apiKey, pdndAccessToken)
      .downloadLegalFactById(iun, legalFactId);
  }

}
