package it.gov.pagopa.pu.send.connector.client;

import static org.junit.jupiter.api.Assertions.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO.HttpMethodEnum;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class SendClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;
  @Mock
  private RestTemplate restTemplateMock;

  private SendClientImpl sendClient;

  @BeforeEach
  public void setUp() {

    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    sendClient = new SendClientImpl(restTemplateBuilderMock, "sendBaseUrl", "apiKey");
  }

  @Test
  public void givenValidRequestWhenPreloadFilesThenVerifyResponse() {

    PreLoadRequestDTO requestDTO = new PreLoadRequestDTO();
    requestDTO.setPreloadIdx("TEST");
    requestDTO.setContentType("application/pdf");
    requestDTO.setSha256("asdsadasdoaisdasldk");
    List<PreLoadRequestDTO> requestList = List.of(requestDTO);

    PreLoadResponseDTO responseDTO = new PreLoadResponseDTO();
    responseDTO.setPreloadIdx("TEST");
    responseDTO.setKey("mock-key");
    responseDTO.setSecret("mock-secret");
    responseDTO.setHttpMethod(HttpMethodEnum.PUT);
    responseDTO.setUrl("https://mock-url.com");
    List<PreLoadResponseDTO> responseList = List.of(responseDTO);

    ResponseEntity<List<PreLoadResponseDTO>> responseEntity = new ResponseEntity<>(responseList, HttpStatus.OK);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<List<PreLoadResponseDTO>>() {})
    )).thenReturn(responseEntity);

    List<PreLoadResponseDTO> result = sendClient.preloadFiles(requestList);

    assertEquals(responseList, result);
  }
}

