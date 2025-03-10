package it.gov.pagopa.pu.send.connector.pagopa.organization.config;


import it.gov.pagopa.pu.organization.client.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.generated.ApiClient;
import it.gov.pagopa.pu.send.config.RestTemplateConfig;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrganizationApisHolder {

  private final RestTemplate restTemplate;
  private final OrganizationApiClientConfig clientConfig;

  private final Map<String, OrganizationApi> organizationApiMap = new ConcurrentHashMap<>();

  public OrganizationApisHolder(
    OrganizationApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
    this.clientConfig = clientConfig;

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ORGANIZATION"));
    }
  }

  public OrganizationApi getOrganizationApi(String accessToken) {
    return organizationApiMap.computeIfAbsent(accessToken, token ->
      new OrganizationApi(buildApiClient(token)));
  }

  private ApiClient buildApiClient(String accessToken) {
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(() -> accessToken);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    return apiClient;
  }
}

