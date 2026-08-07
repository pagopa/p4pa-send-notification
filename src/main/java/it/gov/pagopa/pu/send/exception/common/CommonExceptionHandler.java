package it.gov.pagopa.pu.send.exception.common;

import it.gov.pagopa.pu.send.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoderService;
import it.gov.pagopa.pu.send.util.Utilities;
import jakarta.persistence.RollbackException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

@Slf4j
public class CommonExceptionHandler {

  private static final ExceptionMessageTranscoderService exceptionMessageTranscoderService = new ExceptionMessageTranscoderService();

//region Spring Data
  @ExceptionHandler({TransactionException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleTransactionException(TransactionException ex, HttpServletRequest request) {
    if (ex.getCause() instanceof RollbackException rollbackException && rollbackException.getCause() instanceof ValidationException validationException) {
      return handleViolationException(validationException, request);
    } else {
      return handleRuntimeException(ex, request);
    }
  }

  @ExceptionHandler({DataIntegrityViolationException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleDataIntegrityViolationException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.CONFLICT, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_CONFLICT);
  }

  @ExceptionHandler({CannotAcquireLockException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleCannotAcquireLockException(CannotAcquireLockException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.TOO_MANY_REQUESTS, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_TOO_MANY_REQUESTS);
  }
//endregion

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleConflictException(ConflictException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_CONFLICT);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.FORBIDDEN, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_FORBIDDEN);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class, InvalidValueException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler(NotAuthorizedException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleNotAuthorizedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.UNAUTHORIZED, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_UNAUTHORIZED);
  }

  @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
  public ResponseEntity<SendNotificationErrorDTO> handleInvokedHttpClientTooManyRequestsError(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.TOO_MANY_REQUESTS, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_TOO_MANY_REQUESTS);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatusCode httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    SendNotificationErrorDTO.CategoryEnum errorCode = SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = errorResponse.getStatusCode();
      if (httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleResourceNotFoundException(NotFoundException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_NOT_FOUND);
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_GENERIC_ERROR);
  }

  @ExceptionHandler({AuthorizationDeniedException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleAuthorizationDeniedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.FORBIDDEN, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_FORBIDDEN);
  }

  public static ResponseEntity<SendNotificationErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus, SendNotificationErrorDTO.CategoryEnum errorEnum) {
    logException(ex, request, httpStatus);

    ExceptionMessageTranscoded code2message = buildReturnedMessage(ex);

    String code = Objects.requireNonNullElse(code2message.getCode(), errorEnum.getValue());
    String message = code2message.getMessage();
    List<ErrorFieldDTO> fields = code2message.getFields();

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new SendNotificationErrorDTO(errorEnum, code, message, fields, Utilities.getTraceId()));
  }

  public static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
    boolean printStackTrace = httpStatus.is5xxServerError();
    Level logLevel = printStackTrace ? Level.ERROR : Level.INFO;
    log.makeLoggingEventBuilder(logLevel)
      .log("A {} occurred handling request {}: HttpStatus {} - {}",
        ex.getClass(),
        getRequestDetails(request),
        httpStatus.value(),
        ex.getMessage(),
        printStackTrace ? ex : null
      );
    if (!printStackTrace && log.isDebugEnabled() && ex.getCause() != null) {
      log.debug("CausedBy: ", ex.getCause());
    }
  }

  private static ExceptionMessageTranscoded buildReturnedMessage(Exception ex) {
    return exceptionMessageTranscoderService.transcode(ex);
  }

  public static String getRequestDetails(HttpServletRequest request) {
    String method = Objects.requireNonNullElse(request.getMethod(), "")
      .replace('\n', '_')
      .replace('\r', '_');
    String requestUri = Objects.requireNonNullElse(request.getRequestURI(), "")
      .replace('\n', '_')
      .replace('\r', '_');
    return "%s %s".formatted(method, requestUri);
  }
}
