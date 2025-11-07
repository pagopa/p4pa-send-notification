package it.gov.pagopa.pu.send.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import it.gov.pagopa.pu.send.util.Utilities;
import jakarta.persistence.RollbackException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class SendNotificationExceptionHandler {

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatusCode httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    SendNotificationErrorDTO.CodeEnum errorCode = SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = errorResponse.getStatusCode();
      if (httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }

  @ExceptionHandler({TransactionException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleTransactionException(TransactionException ex, HttpServletRequest request) {
    if (ex.getCause() instanceof RollbackException rollbackException && rollbackException.getCause() instanceof ValidationException validationException) {
      return handleViolationException(validationException, request);
    } else {
      return handleRuntimeException(ex, request);
    }
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_GENERIC_ERROR);
  }

  @ExceptionHandler(UnknownDebtPositionException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleUnknownDebtPositionException(UnknownDebtPositionException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_NOT_FOUND);
  }

  @ExceptionHandler(StatusAlreadyProcessedException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleStatusAlreadyProcessedException(StatusAlreadyProcessedException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.ALREADY_REPORTED, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_ALREADY_PROCESSED);
  }

  @ExceptionHandler(InvalidStatusException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleInvalidStatusException(InvalidStatusException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({SendNotificationNotFoundException.class, SendNotificationFileNotFoundException.class, NotFoundException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({InvalidSignatureException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleInvalidSignatureException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({UploadFileException.class, DeleteFileException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleFileException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, SendNotificationErrorDTO.CodeEnum.SEND_NOTIFICATION_GENERIC_ERROR);
  }

  static ResponseEntity<SendNotificationErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus, SendNotificationErrorDTO.CodeEnum errorEnum) {
    logException(ex, request, httpStatus);

    String message = buildReturnedMessage(ex);

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new SendNotificationErrorDTO(errorEnum, message, Utilities.getTraceId()));
  }

  private static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
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

  private static String buildReturnedMessage(Exception ex) {
    switch (ex) {
      case HttpMessageNotReadableException httpMessageNotReadableException -> {
        if (httpMessageNotReadableException.getCause() instanceof JsonMappingException jsonMappingException) {
          return "Cannot parse body. " +
            jsonMappingException.getPath().stream()
              .map(JsonMappingException.Reference::getFieldName)
              .collect(Collectors.joining(".")) +
            ": " + jsonMappingException.getOriginalMessage();
        }
        return "Required request body is missing";
      }
      case MethodArgumentNotValidException methodArgumentNotValidException -> {
        return "Invalid request content." +
          methodArgumentNotValidException.getBindingResult()
            .getAllErrors().stream()
            .map(e -> " " +
              (e instanceof FieldError fieldError ? fieldError.getField() : e.getObjectName()) +
              ": " + e.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(";"));
      }
      case ConstraintViolationException constraintViolationException -> {
        return "Invalid request content." +
          constraintViolationException.getConstraintViolations()
            .stream()
            .map(e -> " " + e.getPropertyPath() + ": " + e.getMessage())
            .sorted()
            .collect(Collectors.joining(";"));
      }
      default -> {
        return ex.getMessage();
      }
    }
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }

}
