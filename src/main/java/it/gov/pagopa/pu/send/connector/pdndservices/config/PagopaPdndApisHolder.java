package it.gov.pagopa.pu.send.connector.pdndservices.config;

import it.gov.pagopa.pu.pdndservices.client.generated.P4paPdndApi;
import it.gov.pagopa.pu.pdndservices.dto.generated.PdndServicesErrorDTO;
import it.gov.pagopa.pu.pdndservices.generated.ApiClient;
import it.gov.pagopa.pu.pdndservices.generated.BaseApi;
import it.gov.pagopa.pu.send.config.rest.HttpClientErrorJsonBodyHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
public class PagopaPdndApisHolder {

  private final P4paPdndApi pdndApi;
  private final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

  public PagopaPdndApisHolder(
    PagopaPdndApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = buildApiClient(restTemplate, clientConfig);

    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "PDND-SERVICES", clientConfig.isPrintBodyWhenError(),
      PdndServicesErrorDTO.class, PdndServicesErrorDTO::getCode, PdndServicesErrorDTO::getMessage)
    );

    this.pdndApi = new P4paPdndApi(apiClient);
  }

  @PreDestroy
  public void unload() {tokenHolder.remove();
  }

  public P4paPdndApi getP4paPdndApiByApiKey(String accessToken) {
    return getApi(accessToken, pdndApi);
  }


  private ApiClient buildApiClient(RestTemplate restTemplate, PagopaPdndApiClientConfig clientConfig) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(tokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    tokenHolder.set(accessToken);
    return api;
  }

}
