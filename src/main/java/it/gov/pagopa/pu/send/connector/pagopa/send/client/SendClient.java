package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.mapper.SendStreamMapper;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.repository.SendStreamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;

  private final SendStreamRepository sendStreamRepository;
  private final SendStreamMapper sendStreamMapper;

  public SendClient(PagopaSendApisHolder apisHolder, SendStreamRepository sendStreamRepository, SendStreamMapper sendStreamMapper) {
    this.apisHolder = apisHolder;
    this.sendStreamRepository = sendStreamRepository;
    this.sendStreamMapper = sendStreamMapper;
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

  public StreamMetadataResponseV25DTO createStream(StreamCreationRequestV25DTO createStreamRequest, String orgIpaCode, String apikey, String pdndAccessToken){
    List<SendStream> sendStreamList = sendStreamRepository.findByIpaCode(orgIpaCode);
    if(sendStreamList.isEmpty()) {
      StreamMetadataResponseV25DTO streamMetadataResponseV25DTO =
        apisHolder.getStreamsApi(apikey, pdndAccessToken)
          .createEventStreamV25(createStreamRequest);
      sendStreamRepository.save(sendStreamMapper.mapToSendStream(streamMetadataResponseV25DTO, orgIpaCode));
      return streamMetadataResponseV25DTO;
    }
    return sendStreamMapper.mapToStreamMetadataResponseV25DTO(sendStreamList.getFirst());
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
