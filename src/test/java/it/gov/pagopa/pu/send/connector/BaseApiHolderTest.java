package it.gov.pagopa.pu.send.connector;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class BaseApiHolderTest {

  public enum AUTH_TYPE {
    API_KEY,
    BEARER
  }

  @Mock
  protected RestTemplate restTemplateMock;

  protected <T> void assertAuthenticationShouldBeSetInThreadSafeMode(Function<String, T> apiInvoke, Class<T> apiReturnedType, Runnable apiUnloader) throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(apiInvoke, apiReturnedType, apiUnloader, AUTH_TYPE.BEARER, HttpHeaders.AUTHORIZATION);
  }

  protected <T> void assertAuthenticationShouldBeSetInThreadSafeMode(Function<String, T> apiInvoke, Class<T> apiReturnedType, Runnable apiUnloader, AUTH_TYPE authType, String authHeader) throws InterruptedException {
    // Configuring useCases in a single thread
    List<Pair<String, T>> useCases = IntStream.rangeClosed(0, 100)
      .mapToObj(i -> {
        try {
          String auth = "auth" + i;
          String authPrefix = AUTH_TYPE.BEARER.equals(authType) ? "Bearer " : "";
          T expectedResult = apiReturnedType.getConstructor().newInstance();

          Mockito.doReturn(ResponseEntity.ok(expectedResult))
            .when(restTemplateMock)
            .exchange(
              Mockito.argThat(req ->
                req.getHeaders().getOrDefault(authHeader, Collections.emptyList()).getFirst()
                  .equals(authPrefix + auth)),
              Mockito.eq(apiReturnedType));
          return Pair.of(auth, expectedResult);
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .toList();

    try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
      executorService.invokeAll(useCases.stream()
        .map(p -> (Callable<?>) () -> {
          // Given
          String accessToken = p.getKey();
          T expectedResult = p.getValue();

          // When
          T result = apiInvoke.apply(accessToken);

          // Then
          Assertions.assertSame(expectedResult, result);
          return true;
        })
        .toList());
    }

    apiUnloader.run();

    Mockito.verify(restTemplateMock, Mockito.times(useCases.size()))
      .exchange(Mockito.any(), Mockito.<ParameterizedTypeReference<?>>any());
  }
}
