package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.send.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.send.service.SendFacadeService;
import it.gov.pagopa.pu.send.util.SecurityUtilsTest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SendStreamControllerTest {

  @Mock
  private SendFacadeService sendFacadeServiceMock;

  @InjectMocks
  private SendStreamController sendStreamController;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }
  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void givenOrganizationIdAndStreamIdWhenGetStreamEventsThenOk(){
    Long organizationId = 1L;
    String streamId = "STREAMID";
    List<ProgressResponseElementV28DTO> expectedResult = List.of();

    Mockito.when(sendFacadeServiceMock.getStreamEvents(streamId, organizationId, accessToken))
      .thenReturn(expectedResult);

    ResponseEntity<List<ProgressResponseElementV28DTO>> response = sendStreamController
      .getStreamEvents(organizationId, streamId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResult, response.getBody());
  }

  @Test
  void givenOrganizationIdWhenGetStreamThenOk(){
    String streamId = "streamId";
    SendStreamDTO expectedResult = new SendStreamDTO();

    Mockito.when(sendFacadeServiceMock.getStream(streamId, accessToken))
      .thenReturn(expectedResult);

    ResponseEntity<SendStreamDTO> actualResponse = sendStreamController
      .getStream(streamId);

    Assertions.assertNotNull(actualResponse);
    Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    Assertions.assertSame(expectedResult, actualResponse.getBody());
  }

  @Test
  void givenValidInputWhenUpdateStreamLastEventIdThenOk(){
    //GIVEN
    String streamId = "streamId";
    String lastEventId = "lastEventId";

    Mockito.doNothing().when(sendFacadeServiceMock)
      .updateStreamLastEventId(streamId, lastEventId);

    //WHEN
    ResponseEntity<Void> actualResponse = sendStreamController.updateStreamLastEventId(streamId, lastEventId);

    //THEN
    Assertions.assertNotNull(actualResponse);
    Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
  }
}
