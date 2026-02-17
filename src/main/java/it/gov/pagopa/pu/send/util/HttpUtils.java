package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.config.rest.HttpClientConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    private HttpUtils() {
    }

    public static PoolingHttpClientConnectionManagerBuilder getPooledConnectionManagerBuilder(HttpClientConfig httpClientConfig, TlsSocketStrategy tlsSocketStrategy) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setMaxConnPerRoute(httpClientConfig.getConnectionPool().getSizePerRoute())
                .setMaxConnTotal(httpClientConfig.getConnectionPool().getSize())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofMilliseconds(httpClientConfig.getTimeout().getReadMillis()))
                        .setConnectTimeout(Timeout.ofMilliseconds(httpClientConfig.getTimeout().getConnectMillis()))
                        .setTimeToLive(TimeValue.ofMinutes(httpClientConfig.getConnectionPool().getTimeToLiveMinutes()))
                        .build());
    }

    public static HttpComponentsClientHttpRequestFactoryBuilder buildPooledConnection(HttpClientConfig httpClientConfig, TlsSocketStrategy tlsSocketStrategy) {
        return ClientHttpRequestFactoryBuilder.httpComponents()
                .withHttpClientCustomizer(configurer -> configurer
                        .setConnectionManager(getPooledConnectionManagerBuilder(httpClientConfig, tlsSocketStrategy).build()));
    }

    public static class HttpPreSignedGetRequestException extends RuntimeException {
        public HttpPreSignedGetRequestException(String message, Throwable cause) {
          super(message, cause);
        }
    }

    public static byte[] downloadFromPreSignedUrl(URI preSignedURI) {
        RequestConfig config = RequestConfig.custom()
          .setConnectionRequestTimeout(10, TimeUnit.of(ChronoUnit.SECONDS))
          .setResponseTimeout(30, TimeUnit.of(ChronoUnit.SECONDS))
          .build();
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
          HttpGet httpGet = new HttpGet(preSignedURI);
          return httpClient.execute(
            httpGet,
            response ->
              response.getEntity().getContent().readAllBytes()
          );
        } catch (Exception e) {
          String formattedErrorMessage = "Error in downloading file %s".formatted(preSignedURI.getScheme() + preSignedURI.getAuthority() + preSignedURI.getPath());
          throw new HttpPreSignedGetRequestException(formattedErrorMessage, e);
        }
    }
}
