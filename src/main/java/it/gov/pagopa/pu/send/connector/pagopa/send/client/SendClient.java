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

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, String apiKey, String voucherToken) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, String apiKey, String voucherToken) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey, voucherToken)
      .sendNewNotificationV24(newNotificationRequestV24DTO);
  }

  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, String apiKey, String voucherToken) {
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey, voucherToken)
      .retrieveNotificationRequestStatusV24(notificationRequestId, null, null);
  }

  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, String apiKey, String voucherToken) {
    return apisHolder.getNotificationPriceApi(apiKey, voucherToken).retrieveNotificationPriceV23(paTaxId, noticeCode);
  }

}
