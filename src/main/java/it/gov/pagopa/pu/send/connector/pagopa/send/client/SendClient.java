package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;

  public SendClient(PagopaSendApisHolder apisHolder) {
    this.apisHolder = apisHolder;
  }

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, String apiKey, String pdndAccessToken) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey, pdndAccessToken)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV25DTO newNotificationRequestV25DTO, String apiKey, String pdndAccessToken) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey, pdndAccessToken)
      .sendNewNotificationV25(newNotificationRequestV25DTO);
  }

  public NewNotificationRequestStatusResponseV25DTO notificationStatus(String notificationRequestId, String apiKey, String pdndAccessToken) {
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey, pdndAccessToken)
      .retrieveNotificationRequestStatusV25(notificationRequestId, null, null);
  }

  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, String apiKey, String pdndAccessToken) {
    return apisHolder.getNotificationPriceApi(apiKey, pdndAccessToken).retrieveNotificationPriceV23(paTaxId, noticeCode);
  }

  public StreamMetadataResponseV28DTO createStream(StreamCreationRequestV28DTO createStreamRequest, String apikey, String pdndAccessToken){
    return apisHolder.getStreamsApi(apikey, pdndAccessToken)
      .createEventStreamV28(createStreamRequest);
  }

  public List<StreamListElementDTO> getStreams(String apikey, String pdndAccessToken){
    return apisHolder.getStreamsApi(apikey, pdndAccessToken).listEventStreamsV28();
  }

  public List<ProgressResponseElementV28DTO> getStreamEvents(String streamId, String lastEventId, String apiKey, String pdndAccessToken){
    return apisHolder.getEventsApi(apiKey, pdndAccessToken)
      .consumeEventStreamV28(UUID.fromString(streamId), lastEventId);
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
