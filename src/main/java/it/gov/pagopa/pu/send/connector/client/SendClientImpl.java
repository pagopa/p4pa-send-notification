package it.gov.pagopa.pu.send.connector.client;

import it.gov.pagopa.pu.send.connector.send.generated.ApiClient;
import it.gov.pagopa.pu.send.connector.send.generated.api.NewNotificationApi;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SendClientImpl implements SendClient{

  private final NewNotificationApi newNotificationApi;

  public SendClientImpl(RestTemplateBuilder restTemplateBuilder,
    @Value("${app.send.base-url}") String sendBaseUrl,
    @Value("${app.send.api-key}") String apiKey) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(sendBaseUrl);
    // TODO P4ADEV-2110 change static x-api-key with SIL's api-key
    apiClient.addDefaultHeader("x-api-key", apiKey);
    newNotificationApi = new NewNotificationApi(apiClient);
  }

  @Override
  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO) {
    return newNotificationApi.presignedUploadRequest(preLoadRequestDTO);
  }

  @Override
  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO) {
    return newNotificationApi.sendNewNotificationV24(newNotificationRequestV24DTO);
  }
}
