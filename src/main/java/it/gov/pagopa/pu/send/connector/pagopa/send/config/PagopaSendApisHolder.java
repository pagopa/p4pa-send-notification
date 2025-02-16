package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.config.RestTemplateConfig;
import it.gov.pagopa.pu.send.connector.send.generated.ApiClient;
import it.gov.pagopa.pu.send.connector.send.generated.api.NewNotificationApi;
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

}
