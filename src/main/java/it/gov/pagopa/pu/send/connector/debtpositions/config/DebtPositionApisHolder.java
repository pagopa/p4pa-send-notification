package it.gov.pagopa.pu.send.connector.debtpositions.config;


import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.generated.ApiClient;
import it.gov.pagopa.pu.debtpositions.generated.BaseApi;
import it.gov.pagopa.pu.send.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.send.connector.debtpositions.mapper.DebtPositionErrorDTOMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class DebtPositionApisHolder {

  private final DebtPositionApi debtPositionApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public DebtPositionApisHolder(
    DebtPositionApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = buildApiClient(restTemplate, clientConfig);

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "DEBT-POSITIONS", clientConfig.isPrintBodyWhenError(),
      DebtPositionErrorDTO.class, DebtPositionErrorDTOMapper::map)
    );

    this.debtPositionApi = new DebtPositionApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public DebtPositionApi getDebtPositionApi(String accessToken) {
    return getApi(accessToken, debtPositionApi);
  }

  private ApiClient buildApiClient(RestTemplate restTemplate, DebtPositionApiClientConfig clientConfig) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}

