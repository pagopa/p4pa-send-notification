package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.config.rest.HttpClientConfig;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;

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
        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

        ArgumentCaptor<HttpClientResponseHandler<ClassicHttpResponse>> classicHttpResponseArgumentCaptor =
          ArgumentCaptor.forClass(HttpClientResponseHandler.class);

        byte[] expectedBytes = "test".getBytes();
        Mockito.when(
          httpClientMock.execute(
            Mockito.isA(HttpGet.class),
            classicHttpResponseArgumentCaptor.capture()
          )
        ).thenAnswer(i -> expectedBytes);

        URI uri = Mockito.mock(URI.class);

        //WHEN
        byte[] actualBytes = HttpUtils.downloadFromPreSignedUrl(uri, httpClientMock);

        //THEN
        Mockito.verify(httpClientMock)
          .execute(
            Mockito.isA(HttpGet.class),
            Mockito.eq(classicHttpResponseArgumentCaptor.getValue())
          );
        Assertions.assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    void givenExceptionWhenDownloadFromPreSignedUrlThenThrowHttpPreSignedGetRequestException() throws IOException {
      //GIVEN
      URI uri = Mockito.mock(URI.class);
        Mockito.when(uri.getPath()).thenReturn("/path/id/123");
      CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

      Mockito.doThrow(new IOException())
        .when(httpClientMock)
        .execute(
          Mockito.isA(HttpGet.class),
          Mockito.isA(HttpClientResponseHandler.class)
        );

      //WHEN
      HttpUtils.HttpPreSignedGetRequestException httpPreSignedGetRequestException =
        Assertions.assertThrows(
          HttpUtils.HttpPreSignedGetRequestException.class,
          () -> HttpUtils.downloadFromPreSignedUrl(uri, httpClientMock)
        );

      //THEN
      Assertions.assertNotNull(httpPreSignedGetRequestException);
      Assertions.assertEquals(
        "Error in downloading file %s".formatted("/path/id/123"),
        httpPreSignedGetRequestException.getMessage()
      );
    }

    @Test
    void givenInvalidResponseStatusWhenDownloadFromPreSignedUrlThenThrowHttpPreSignedGetRequestException() throws IOException {
      //GIVEN
      URI uri = Mockito.mock(URI.class);
      Mockito.when(uri.getPath()).thenReturn("/path/id/123");
      CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

      ClassicHttpResponse classicHttpResponseMock = Mockito.mock(ClassicHttpResponse.class);
      Mockito.when(classicHttpResponseMock.getCode())
        .thenReturn(400);

      Mockito.when(httpClientMock.execute(
          Mockito.isA(HttpGet.class),
          Mockito.isA(HttpClientResponseHandler.class)
        )).thenAnswer(i -> ((HttpClientResponseHandler)i.getArgument(1)).handleResponse(classicHttpResponseMock));

      //WHEN
      HttpUtils.HttpPreSignedGetRequestException httpPreSignedGetRequestException =
        Assertions.assertThrows(
          HttpUtils.HttpPreSignedGetRequestException.class,
          () -> HttpUtils.downloadFromPreSignedUrl(uri, httpClientMock)
        );

      //THEN
      Assertions.assertNotNull(httpPreSignedGetRequestException);
      Assertions.assertEquals(
        "Error in downloading file %s".formatted("/path/id/123"),
        httpPreSignedGetRequestException.getMessage()
      );
      Assertions.assertNotNull(httpPreSignedGetRequestException.getCause());
      Assertions.assertEquals(
        new HttpResponseException(400, "Unexpected response status: 400").getMessage(),
        httpPreSignedGetRequestException.getCause().getMessage()
      );
    }

    @Test
    void givenValidResponseStatusWhenDownloadFromPreSignedUrlThenOk() throws IOException {
      //GIVEN
      URI uri = Mockito.mock(URI.class);
      Mockito.when(uri.getPath()).thenReturn("/path/id/123");
      byte[] expectedBytes = "test".getBytes();

      HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
      Mockito.when(httpEntityMock.getContent())
        .thenReturn(new ByteArrayInputStream(expectedBytes));

      ClassicHttpResponse classicHttpResponseMock = Mockito.mock(ClassicHttpResponse.class);
      Mockito.when(classicHttpResponseMock.getCode())
        .thenReturn(200);
      Mockito.when(classicHttpResponseMock.getEntity())
        .thenReturn(httpEntityMock);

      CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
      Mockito.when(httpClientMock.execute(
        Mockito.isA(HttpGet.class),
        Mockito.isA(HttpClientResponseHandler.class)
      )).thenAnswer(i -> ((HttpClientResponseHandler)i.getArgument(1)).handleResponse(classicHttpResponseMock));

      //WHEN
      byte[] actualBytes = HttpUtils.downloadFromPreSignedUrl(uri, httpClientMock);

      //THEN
      Assertions.assertArrayEquals(expectedBytes, actualBytes);
    }

}
