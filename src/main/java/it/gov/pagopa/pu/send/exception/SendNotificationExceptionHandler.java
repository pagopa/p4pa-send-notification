package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import it.gov.pagopa.pu.send.exception.common.CommonExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class SendNotificationExceptionHandler extends CommonExceptionHandler {

  @ExceptionHandler(UnknownDebtPositionException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleUnknownDebtPositionException(UnknownDebtPositionException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_NOT_FOUND);
  }

  @ExceptionHandler(StatusAlreadyProcessedException.class)
  public ResponseEntity<SendNotificationErrorDTO> handleStatusAlreadyProcessedException(StatusAlreadyProcessedException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.ALREADY_REPORTED, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_ALREADY_PROCESSED);
  }

  @ExceptionHandler({InvalidStatusException.class, FileAlreadyExistsException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleInvalidStatusException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({SendNotificationNotFoundException.class, SendNotificationFileNotFoundException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({InvalidSignatureException.class, InvalidTaxonomyException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleInvalidSignatureException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_BAD_REQUEST);
  }

  @ExceptionHandler({UploadFileException.class, DeleteFileException.class})
  public ResponseEntity<SendNotificationErrorDTO> handleFileException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_GENERIC_ERROR);
  }

}
