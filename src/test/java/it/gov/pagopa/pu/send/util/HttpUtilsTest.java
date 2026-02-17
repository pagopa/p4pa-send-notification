package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.config.rest.HttpClientConfig;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Path;

class HttpUtilsTest {

    @Test
    void whenGetPooledConnectionManagerBuilderThenReturnConfiguredConnectionManager() throws NoSuchFieldException, IllegalAccessException {
        // Given
        HttpClientConfig httpClientConfig = buildTestHttpClientConfig();
        TlsSocketStrategy tlsSocketStrategy = Mockito.mock(TlsSocketStrategy.class);

        // When
        PoolingHttpClientConnectionManager result = HttpUtils.getPooledConnectionManagerBuilder(httpClientConfig, tlsSocketStrategy).build();

        // Then
        assertHttpClientConnectionManager(result);
    }

    @Test
    void whenBuildPooledConnectionThenReturnConfiguredConnectionManager() throws NoSuchFieldException, IllegalAccessException {
        // Given
        HttpClientConfig httpClientConfig = buildTestHttpClientConfig();
        TlsSocketStrategy tlsSocketStrategy = Mockito.mock(TlsSocketStrategy.class);

        // When
        HttpComponentsClientHttpRequestFactory result = HttpUtils.buildPooledConnection(httpClientConfig, tlsSocketStrategy).build();

        // Then
        HttpClient httpClient = result.getHttpClient();
        Field connManagerField = httpClient.getClass().getDeclaredField("connManager");
        connManagerField.setAccessible(true);
        PoolingHttpClientConnectionManager pooledConnectionManager = (PoolingHttpClientConnectionManager) connManagerField.get(httpClient);
        assertHttpClientConnectionManager(pooledConnectionManager);
    }

    private static HttpClientConfig buildTestHttpClientConfig() {
        return HttpClientConfig.builder()
                .connectionPool(HttpClientConfig.HttpClientConnectionPoolConfig.builder()
                        .size(10)
                        .sizePerRoute(5)
                        .timeToLiveMinutes(3)
                        .build())
                .timeout(HttpClientConfig.HttpClientTimeoutConfig.builder()
                        .connectMillis(1000)
                        .readMillis(3000)
                        .build())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static void assertHttpClientConnectionManager(PoolingHttpClientConnectionManager result) throws NoSuchFieldException, IllegalAccessException {
        Assertions.assertEquals(10, result.getMaxTotal());
        Assertions.assertEquals(5, result.getDefaultMaxPerRoute());

        Field connectionConfigResolverField = PoolingHttpClientConnectionManager.class.getDeclaredField("connectionConfigResolver");
        connectionConfigResolverField.setAccessible(true);
        Resolver<HttpRoute, ConnectionConfig> connectionConfigResolver = (Resolver<HttpRoute, ConnectionConfig>) connectionConfigResolverField.get(result);
        ConnectionConfig connectionConfig = connectionConfigResolver.resolve(null);

        Assertions.assertEquals(Timeout.ofMilliseconds(1_000), connectionConfig.getConnectTimeout());
        Assertions.assertEquals(Timeout.ofMilliseconds(3_000), connectionConfig.getSocketTimeout());
        Assertions.assertEquals(Timeout.ofMilliseconds(180_000), connectionConfig.getTimeToLive());
    }

    @Test
    void givenCorrectRequestWhenDownloadFromPreSignedUrlThenOk() throws IOException {
        //GIVEN
        try (MockedStatic<HttpClients> httpClientStaticMock = Mockito.mockStatic(HttpClients.class);
             CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class)) {

          HttpClientBuilder httpClientBuilderMock = Mockito.mock(HttpClientBuilder.class);

          httpClientStaticMock.when(HttpClients::custom)
            .thenReturn(httpClientBuilderMock);
          Mockito.when(httpClientBuilderMock.setDefaultRequestConfig(Mockito.isA(RequestConfig.class)))
            .thenReturn(httpClientBuilderMock);
          Mockito.when(httpClientBuilderMock.build())
            .thenReturn(httpClientMock);

          ArgumentCaptor<HttpClientResponseHandler<ClassicHttpResponse>> classicHttpResponseArgumentCaptor =
            ArgumentCaptor.forClass(HttpClientResponseHandler.class);

          byte[] expectedBytes = new byte[0];
          Mockito.when(
            httpClientMock.execute(
              Mockito.isA(HttpGet.class),
              classicHttpResponseArgumentCaptor.capture()
            )
          ).thenAnswer(i -> expectedBytes);

          URI uri = Mockito.mock(URI.class);

          //WHEN
          byte[] bytes = HttpUtils.downloadFromPreSignedUrl(uri);

          //THEN
          Mockito.verify(httpClientMock)
            .execute(
              Mockito.isA(HttpGet.class),
              Mockito.eq(classicHttpResponseArgumentCaptor.getValue())
            );
          Assertions.assertEquals(expectedBytes, bytes);
        }
    }

    @Test
    void givenExceptionWhenDownloadFromPreSignedUrlThenThrowHttpPreSignedGetRequestException() {
        //GIVEN
        try (MockedStatic<HttpClients> httpClientStaticMock = Mockito.mockStatic(HttpClients.class)) {
          httpClientStaticMock.when(HttpClients::custom)
            .thenThrow(new RuntimeException());

          URI uri = Mockito.mock(URI.class);
          Mockito.when(uri.getScheme()).thenReturn("http://");
          Mockito.when(uri.getAuthority()).thenReturn("pagopa.com");
          Mockito.when(uri.getPath()).thenReturn("/path/id/123");

          //WHEN
          HttpUtils.HttpPreSignedGetRequestException httpPreSignedGetRequestException =
            Assertions.assertThrows(HttpUtils.HttpPreSignedGetRequestException.class, () -> HttpUtils.downloadFromPreSignedUrl(uri));

          //THEN
          Assertions.assertEquals(
            "Error in downloading file %s".formatted("http://pagopa.com/path/id/123"),
            httpPreSignedGetRequestException.getMessage()
          );
        }
    }

}
