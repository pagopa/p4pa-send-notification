package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;
  private final String apiKey;

  public SendClient(
    @Value("${rest.send.api-key}") String apiKey,

    PagopaSendApisHolder apisHolder
  ) {
    // TODO P4ADEV-2110 change static x-api-key with SIL's api-key
    this.apiKey = apiKey;
    this.apisHolder = apisHolder;
  }

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO) {
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .sendNewNotificationV24(newNotificationRequestV24DTO);
  }

  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId) {
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey)
      .retrieveNotificationRequestStatusV24(notificationRequestId, null, null);
  }

}
