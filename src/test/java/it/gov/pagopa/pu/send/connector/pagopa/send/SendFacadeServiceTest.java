package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendFacadeServiceTest {

  @Mock
  private SendClient clientMock;

  private SendService service;

  @BeforeEach
  void init(){
    service = new SendServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenPreloadFilesThenInvokeClient(){
    // Given
    List<PreLoadRequestDTO> request = List.of();
    List<PreLoadResponseDTO> expectedResult = List.of();

    Mockito.when(clientMock.preloadFiles(Mockito.same(request)))
      .thenReturn(expectedResult);

    // When
    List<PreLoadResponseDTO> result = service.preloadFiles(request);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenDeliveryNotificationThenInvokeClient(){
    // Given
    NewNotificationRequestV24DTO request = new NewNotificationRequestV24DTO();
    NewNotificationResponseDTO expectedResult = new NewNotificationResponseDTO();

    Mockito.when(clientMock.deliveryNotification(Mockito.same(request)))
      .thenReturn(expectedResult);

    // When
    NewNotificationResponseDTO result = service.deliveryNotification(request);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
