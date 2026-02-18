package it.gov.pagopa.pu.send.config.rest;

import it.gov.pagopa.pu.send.util.HttpUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClosableHttpClientConfig {
  @Bean
  public CloseableHttpClient closeableHttpClient(HttpClientConfig defaultHttpClientConfig) {
    var connectionManager = HttpUtils.getPooledConnectionManagerBuilder(
      defaultHttpClientConfig,
      DefaultClientTlsStrategy.createSystemDefault()
    ).build();

    return HttpClients.custom()
      .setConnectionManager(connectionManager)
      .evictIdleConnections(TimeValue.ofMinutes(defaultHttpClientConfig.getConnectionPool().getTimeToLiveMinutes()))
      .build();
  }
}
