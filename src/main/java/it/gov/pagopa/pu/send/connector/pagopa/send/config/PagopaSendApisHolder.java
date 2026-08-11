package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.send.generated.ApiClient;
import it.gov.pagopa.send.client.generated.*;
import it.gov.pagopa.send.dto.generated.ProblemDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Objects;
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

  public PagopaSendApisHolder(
    PagopaSendApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ){
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "SEND", clientConfig.isPrintBodyWhenError(),
      ProblemDTO.class, ProblemDTO::getTitle, ProblemDTO::getDetail)
    );
  }

  public NewNotificationApi getNewNotificationApiByApiKey(String apiKey, String pdndAccessToken) {
    String cacheKey = apiKey+Objects.toString(pdndAccessToken,"");
    return newNotificationApiMap.computeIfAbsent(cacheKey, key ->
      new NewNotificationApi(buildApiClient(apiKey, pdndAccessToken)));
  }

  public SenderReadB2BApi getSenderReadB2BApiByApiKey(String apiKey, String pdndAccessToken) {
    String cacheKey = apiKey+Objects.toString(pdndAccessToken,"");
    return senderReadB2BApiMap.computeIfAbsent(cacheKey, key ->
      new SenderReadB2BApi(buildApiClient(apiKey, pdndAccessToken)));
  }

  public NotificationPriceV23Api getNotificationPriceApi(String apiKey, String pdndAccessToken) {
    String cacheKey = apiKey+Objects.toString(pdndAccessToken,"");
    return notificationPriceApiMap.computeIfAbsent(cacheKey, key ->
      new NotificationPriceV23Api(buildApiClient(apiKey, pdndAccessToken)));
  }

  public StreamsApi getStreamsApi(String apiKey, String pdndAccessToken) {
    String cacheKey = apiKey+Objects.toString(pdndAccessToken,"");
    return streamsApiMap.computeIfAbsent(cacheKey, key ->
      new StreamsApi(buildApiClient(apiKey, pdndAccessToken)));
  }

  public EventsApi getEventsApi(String apiKey, String pdndAccessToken){
    String cacheKey = apiKey+Objects.toString(pdndAccessToken,"");
    return eventsApiMap.computeIfAbsent(cacheKey, key ->
      new EventsApi(buildApiClient(apiKey, pdndAccessToken)));
  }

  public LegalFactsApi getLegalFactsApiByApiKey(String apiKey, String pdndAccessToken) {
    String cacheKey = apiKey+Objects.toString(pdndAccessToken,"");
    return legalFactsApiMap.computeIfAbsent(cacheKey, key ->
      new LegalFactsApi(buildApiClient(apiKey, pdndAccessToken)));
  }

  private ApiClient buildApiClient(String apiKey, String pdndAccessToken) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setApiKey(apiKey);
    if (StringUtils.isNotEmpty(pdndAccessToken)) {
      apiClient.setBearerToken(pdndAccessToken);
    }
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }
}
