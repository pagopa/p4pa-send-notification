package it.gov.pagopa.pu.send.connector.pagopa.send;


import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendUploadClient;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SendSendUploadFacadeServiceTest {

  @Mock
  private SendUploadClient clientMock;

  private SendUploadService service;

  @BeforeEach
  void init(){
    service = new SendUploadServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenPreloadFilesThenInvokeClient(){
    // Given
    DocumentDTO document = new DocumentDTO();
    byte[] fileBytes = new byte[0];
    Optional<String> expectedResult = Optional.empty();

    Mockito.when(clientMock.upload(Mockito.same(document), Mockito.same(fileBytes)))
      .thenReturn(expectedResult);

    // When
    Optional<String> result = service.upload(document, fileBytes);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
