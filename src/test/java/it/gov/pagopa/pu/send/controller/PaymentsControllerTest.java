package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.service.PaymentsService;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentsControllerTest {

  @Mock
  private PaymentsService paymentsServiceMock;

  @InjectMocks
  private PaymentsController paymentsController;

  @BeforeEach
  void init(){
    String accessToken = "ACCESSTOKEN";
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void clearContext(){
    SecurityUtilsTest.clearSecurityContext();
    Mockito.verifyNoMoreInteractions(paymentsServiceMock);
  }

  @Test
  void whenNotifyPaymentThenReturnOk() {
    Long organizationId = 1L;
    String nav = "NAV";
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    when(paymentsServiceMock.notifyPayment(organizationId, nav))
      .thenReturn(expectedResult);

    ResponseEntity<SendNotificationDTO> response = paymentsController.notifyPayment(organizationId, nav);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertSame(expectedResult, response.getBody());
  }
}
