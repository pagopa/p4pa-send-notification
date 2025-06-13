package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;

  public SendClient(
    PagopaSendApisHolder apisHolder
  ) {
    this.apisHolder = apisHolder;
  }

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, String apiKey) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, String apiKey) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .sendNewNotificationV24(newNotificationRequestV24DTO);
  }

  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, String apiKey) {
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey)
      .retrieveNotificationRequestStatusV24(notificationRequestId, null, null);
  }

  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, String apiKey) {
    return apisHolder.getNotificationPriceApi(apiKey).retrieveNotificationPriceV23(paTaxId, noticeCode);
  }

}
