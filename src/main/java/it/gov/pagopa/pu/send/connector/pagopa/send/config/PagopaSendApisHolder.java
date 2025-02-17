package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.config.RestTemplateConfig;
import it.gov.pagopa.pu.send.connector.send.generated.ApiClient;
import it.gov.pagopa.pu.send.connector.send.generated.api.NewNotificationApi;
import it.gov.pagopa.pu.send.connector.send.generated.api.SenderReadB2BApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PagopaSendApisHolder {

  private final RestTemplate restTemplate;
  private final PagopaSendApiClientConfig clientConfig;

  private final Map<String, NewNotificationApi> newNotificationApiMap = new ConcurrentHashMap<>();
  private final Map<String, SenderReadB2BApi> senderReadB2BApiMap = new ConcurrentHashMap<>();

  public PagopaSendApisHolder(
    PagopaSendApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("SEND"));
    }
  }

  public NewNotificationApi getNewNotificationApiByApiKey(String apiKey) {
    return newNotificationApiMap.computeIfAbsent(apiKey, key -> {
      ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(clientConfig.getBaseUrl());
      apiClient.setApiKey(key);
      apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
      apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
      return new NewNotificationApi(apiClient);
    });
  }

  public SenderReadB2BApi getSenderReadB2BApiByApiKey(String apiKey) {
    return senderReadB2BApiMap.computeIfAbsent(apiKey, key -> {
      ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(clientConfig.getBaseUrl());
      apiClient.setApiKey(key);
      apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
      apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
      return new SenderReadB2BApi(apiClient);
    });
  }
}
