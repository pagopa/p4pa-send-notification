package it.gov.pagopa.pu.send.exception.transcoder.handler;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoDataIntegrityViolationException;

public class DataIntegrityViolationExceptionMessageTranscoder implements ExceptionMessageTranscoder<DataIntegrityViolationException> {

  @Override
  public ExceptionMessageTranscoded transcode(DataIntegrityViolationException dataIntegrityViolationException) {
    String errorMsg = "Conflict.";
    if(dataIntegrityViolationException.getCause() instanceof ConstraintViolationException hibernateConstraintViolationException) {
      errorMsg += " " + hibernateConstraintViolationException.getSQLException().getMessage();
    }
    if(dataIntegrityViolationException.getCause() instanceof MongoDataIntegrityViolationException mongoDataIntegrityViolationException) {
      errorMsg += " " + mongoDataIntegrityViolationException.getMostSpecificCause().getMessage();
    }
    return new ExceptionMessageTranscoded(
      SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_CONFLICT.getValue(),
      errorMsg,
      null) ;
  }
}
