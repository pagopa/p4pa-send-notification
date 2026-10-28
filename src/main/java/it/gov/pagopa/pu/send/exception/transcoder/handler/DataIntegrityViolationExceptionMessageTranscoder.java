package it.gov.pagopa.pu.send.exception.transcoder.handler;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoDataIntegrityViolationException;

public class DataIntegrityViolationExceptionMessageTranscoder implements ExceptionMessageTranscoder<DataIntegrityViolationException> {

  private static final String UNIQUE_KEY_VIOLATION_CODE = "23505";

  @Override
  public ExceptionMessageTranscoded transcode(DataIntegrityViolationException dataIntegrityViolationException) {
    String errorMsg = "Data integrity violation.";
    boolean isUniqueViolation = false;
    Throwable rootCause = dataIntegrityViolationException.getMostSpecificCause();

    // 1. PostgreSQL
    if (dataIntegrityViolationException.getCause() instanceof ConstraintViolationException hibernateConstraintViolationException) {
      java.sql.SQLException sqlEx = hibernateConstraintViolationException.getSQLException();
      errorMsg += " "+sqlEx.getMessage();
      String sqlState = sqlEx.getSQLState();

      if (UNIQUE_KEY_VIOLATION_CODE.equals(sqlState)) {
        isUniqueViolation = true;
        errorMsg = "Conflict. "+sqlEx.getMessage();
      }
    }
    // 2. MongoDB
    else if (dataIntegrityViolationException.getCause() instanceof MongoDataIntegrityViolationException
      || dataIntegrityViolationException instanceof DuplicateKeyException) {
      errorMsg = "Conflict. "+rootCause.getMessage();
      isUniqueViolation = true;
    }

    String categoryCode = isUniqueViolation
      ? SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_CONFLICT.getValue()
      : SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_BAD_REQUEST.getValue();

    return new ExceptionMessageTranscoded(
      categoryCode,
      errorMsg,
      null);
  }
}
