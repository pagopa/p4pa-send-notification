package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.service.SendFacadeService;
import it.gov.pagopa.pu.send.util.SecurityUtilsTest;
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

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendControllerTest {

  @Mock
  private SendFacadeService sendFacadeServiceMock;

  @InjectMocks
  private SendController sendController;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, "MAPPEDEXTERNALUSERID");
  }
  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void givenSendNotificationIdWhenPreloadFilesRequestThenOk() {
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).preloadFiles(sendNotificationId, accessToken);
    ResponseEntity<Void> response = sendController.preloadSendFile(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenUploadFilesRequestThenOk(){
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).uploadFiles(sendNotificationId);
    ResponseEntity<Void> response = sendController.uploadSendFile(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenDeliveryNotificationRequestThenOk(){
    String sendNotificationId = "12345";
    Mockito.doNothing().when(sendFacadeServiceMock).deliveryNotification(sendNotificationId, accessToken);
    ResponseEntity<Void> response = sendController.deliveryNotification(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdWhenNotificationStatusRequestThenOk(){
    String sendNotificationId = "12345";
    SendNotificationDTO status = new SendNotificationDTO();
    Mockito.when(sendFacadeServiceMock.notificationStatus(sendNotificationId, accessToken)).thenReturn(status);

    ResponseEntity<SendNotificationDTO> response = sendController.notificationStatus(sendNotificationId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(status, response.getBody());
  }

  @Test
  void givenSendNotificationIdAndOrganizationIdWhenRetrieveNotificationDateThenOk() {
    String sendNotificationId = "12345";

    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    Mockito.when(sendFacadeServiceMock.retrieveNotificationDate(sendNotificationId, accessToken))
      .thenReturn(notificationDTO);

    ResponseEntity<SendNotificationDTO> response = sendController.retrieveNotificationDate(sendNotificationId);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void givenSendNotificationIdAndOrganizationIdWhenRetrieveNotificationDateThenNoContent() {
    String sendNotificationId = "12345";
    Mockito.when(sendFacadeServiceMock.retrieveNotificationDate(sendNotificationId, accessToken))
      .thenReturn(null);

    ResponseEntity<SendNotificationDTO> response = sendController.retrieveNotificationDate(sendNotificationId);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void givenOrganizationIdAndNavWhenRetrieveNotificationPriceRequestThenOk(){
    Long organizationId = 1L;
    String nav = "12345";
    NotificationPriceResponseV23DTO price = new NotificationPriceResponseV23DTO();
    Mockito.when(sendFacadeServiceMock.retrieveNotificationPrice(organizationId, nav, accessToken))
      .thenReturn(price);

    ResponseEntity<NotificationPriceResponseV23DTO> response = sendController.retrieveNotificationPrice(organizationId, nav);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(price, response.getBody());
  }

  @Test
  void givenSendNotificationIdWhenRetrieveLegalFactsThenOk() {
    String sendNotificationId = "12345";

    List<LegalFactListElementDTO> expectedLegalFacts = new ArrayList<>();
    Mockito.when(sendFacadeServiceMock.retrieveLegalFacts(sendNotificationId, accessToken))
      .thenReturn(expectedLegalFacts);

    ResponseEntity<List<LegalFactListElementDTO>> actualLegalFactsResponse = sendController.retrieveLegalFacts(sendNotificationId);
    Assertions.assertNotNull(actualLegalFactsResponse);
    Assertions.assertEquals(expectedLegalFacts, actualLegalFactsResponse.getBody());
    Assertions.assertEquals(HttpStatus.OK, actualLegalFactsResponse.getStatusCode());
  }

}
