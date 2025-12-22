package it.gov.pagopa.pu.send.connector.pdnd.config;

import it.gov.pagopa.pu.pdnd.client.generated.P4paPdndApi;
import it.gov.pagopa.pu.pdnd.generated.ApiClient;
import it.gov.pagopa.pu.pdnd.generated.BaseApi;
import it.gov.pagopa.pu.send.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PagopaPdndApisHolder {

  private final P4paPdndApi pdndApi;
  private final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

  public PagopaPdndApisHolder(PagopaPdndApiClientConfig clientConfig, RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = buildApiClient(restTemplate, clientConfig);

    if(clientConfig.isPrintBodyWhenError()){
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("PDND"));
    }

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
