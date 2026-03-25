package it.gov.pagopa.pu.send.connector.organization.config;

import it.gov.pagopa.pu.organization.client.generated.BrokerSearchControllerApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.generated.ApiClient;
import it.gov.pagopa.pu.organization.generated.BaseApi;
import it.gov.pagopa.pu.send.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrganizationApisHolder {

  private final OrganizationApi organizationApi;
  private final OrganizationEntityControllerApi organizationEntityControllerApi;
  private final BrokerSearchControllerApi brokerSearchControllerApi;
  private final OrganizationSearchControllerApi organizationSearchControllerApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public OrganizationApisHolder(OrganizationApiClientConfig clientConfig, RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = buildApiClient(restTemplate, clientConfig);

    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ORGANIZATION"));
    }

    this.organizationApi = new OrganizationApi(apiClient);
    this.organizationEntityControllerApi = new OrganizationEntityControllerApi(apiClient);
    this.brokerSearchControllerApi = new BrokerSearchControllerApi(apiClient);
    this.organizationSearchControllerApi = new OrganizationSearchControllerApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public OrganizationApi getOrganizationApi(String accessToken) {
    return getApi(accessToken, organizationApi);
  }

  public OrganizationEntityControllerApi getOrganizationEntityControllerApi(String accessToken) {
    return getApi(accessToken, organizationEntityControllerApi);
  }

  public BrokerSearchControllerApi getBrokerSearchControllerApi(String accessToken) {
    return getApi(accessToken, brokerSearchControllerApi);
  }

  public  OrganizationSearchControllerApi getOrganizationSearchControllerApi(String accessToken) {
    return getApi(accessToken, organizationSearchControllerApi);
  }

  private ApiClient buildApiClient(RestTemplate restTemplate, OrganizationApiClientConfig clientConfig) {
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

