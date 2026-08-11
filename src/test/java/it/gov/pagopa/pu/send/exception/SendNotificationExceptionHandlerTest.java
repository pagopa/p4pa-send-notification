package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.common.CommonExceptionHandlerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.doThrow;

class SendNotificationExceptionHandlerTest extends CommonExceptionHandlerTest {

  @Test
  void handleInvalidStatusException() throws Exception {
    doThrow(new InvalidStatusException("ERRORCODE", NotificationStatus.WAITING_FILE, NotificationStatus.SENDING)).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Notification status error: Expected: WAITING_FILE, Actual: SENDING"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleStatusAlreadyProcessedException() throws Exception {
    doThrow(new StatusAlreadyProcessedException(NotificationStatus.WAITING_FILE, NotificationStatus.SENDING)).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isAlreadyReported())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_ALREADY_PROCESSED"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("STATUS_ALREADY_PROCESSED"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Expected status is WAITING_FILE, but it has already be processed: actual is SENDING"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleSendNotificationNotFoundException() throws Exception {
    doThrow(new SendNotificationNotFoundException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleSendNotificationFileNotFoundException() throws Exception {
    doThrow(new SendNotificationFileNotFoundException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("FILE_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidSignatureException() throws Exception {
    doThrow(new InvalidSignatureException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INVALID_SIGNATURE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidTaxonomyException() throws Exception {
    doThrow(new InvalidTaxonomyException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INVALID_TAXONOMY_CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleUploadFileExceptionError() throws Exception {
    doThrow(new UploadFileException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleUnknownDebtPositionException() throws Exception {
    doThrow(new UnknownDebtPositionException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleFileAlreadyExistsExceptionError() throws Exception {
    doThrow(new FileAlreadyExistsException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("SEND_NOTIFICATION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }
}
