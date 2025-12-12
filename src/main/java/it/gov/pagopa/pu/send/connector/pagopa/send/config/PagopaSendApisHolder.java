package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.config.rest.RestTemplateConfig;
import it.gov.pagopa.pu.send.connector.send.generated.ApiClient;
import it.gov.pagopa.pu.send.connector.send.generated.api.EventsApi;
import it.gov.pagopa.pu.send.connector.send.generated.api.NewNotificationApi;
import it.gov.pagopa.pu.send.connector.send.generated.api.NotificationPriceV23Api;
import it.gov.pagopa.pu.send.connector.send.generated.api.SenderReadB2BApi;
import it.gov.pagopa.pu.send.connector.send.generated.api.StreamsApi;
import it.gov.pagopa.pu.send.connector.send.generated.api.LegalFactsApi;
import java.util.Objects;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
  private final Map<String, NotificationPriceV23Api> notificationPriceApiMap = new ConcurrentHashMap<>();
  private final Map<String, StreamsApi> streamsApiMap = new ConcurrentHashMap<>();
  private final Map<String, EventsApi> eventsApiMap = new ConcurrentHashMap<>();
  private final Map<String, LegalFactsApi> legalFactsApiMap = new ConcurrentHashMap<>();

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public PagopaSendApisHolder(
    PagopaSendApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("SEND"));
    }
  }

  public NewNotificationApi getNewNotificationApiByApiKey(String apiKey, String pdndAccessToken) {
    bearerTokenHolder.set(pdndAccessToken);
    return newNotificationApiMap.computeIfAbsent(apiKey, key ->
      new NewNotificationApi(buildApiClient(apiKey)));
  }

  public SenderReadB2BApi getSenderReadB2BApiByApiKey(String apiKey, String pdndAccessToken) {
    bearerTokenHolder.set(pdndAccessToken);
    return senderReadB2BApiMap.computeIfAbsent(apiKey, key ->
      new SenderReadB2BApi(buildApiClient(apiKey)));
  }

  public NotificationPriceV23Api getNotificationPriceApi(String apiKey, String pdndAccessToken) {
    bearerTokenHolder.set(pdndAccessToken);
    return notificationPriceApiMap.computeIfAbsent(apiKey, key ->
      new NotificationPriceV23Api(buildApiClient(apiKey)));
  }

  public StreamsApi getStreamsApi(String apiKey, String pdndAccessToken) {
    bearerTokenHolder.set(pdndAccessToken);
    return streamsApiMap.computeIfAbsent(apiKey, key ->
      new StreamsApi(buildApiClient(apiKey)));
  }

  public EventsApi getEventsApi(String apiKey, String pdndAccessToken){
    bearerTokenHolder.set(pdndAccessToken);
    return eventsApiMap.computeIfAbsent(apiKey, key ->
      new EventsApi(buildApiClient(apiKey)));
  }

  public LegalFactsApi getLegalFactsApiByApiKey(String apiKey, String pdndAccessToken) {
    bearerTokenHolder.set(pdndAccessToken);
    return legalFactsApiMap.computeIfAbsent(apiKey, key ->
      new LegalFactsApi(buildApiClient(apiKey)));
  }

  @PreDestroy
  public void unload(){
    bearerTokenHolder.remove();
  }

  private ApiClient buildApiClient(String apiKey) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setApiKey(apiKey);
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }
}
