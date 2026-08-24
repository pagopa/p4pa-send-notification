package it.gov.pagopa.pu.send.exception.transcoder.handler;

import com.mongodb.WriteConcernResult;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoActionOperation;
import org.springframework.data.mongodb.core.MongoDataIntegrityViolationException;

import java.sql.SQLException;

class DataIntegrityViolationExceptionMessageTranscoderTest {

  private final DataIntegrityViolationExceptionMessageTranscoder transcoder = new DataIntegrityViolationExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    DataIntegrityViolationException exception = new DataIntegrityViolationException("message");

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "SEND_NOTIFICATION_CONFLICT",
        "Conflict.",
        null),
      result);
  }

  @Test
  void givenHibernateConstraintViolationExceptionCauseWhenTranscodeThenOk() {
    // Given
    SQLException sqlException = new SQLException("sqlErrorMessage");
    DataIntegrityViolationException exception = new DataIntegrityViolationException("message", new ConstraintViolationException("message", sqlException, "constraintName"));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "SEND_NOTIFICATION_CONFLICT",
        "Conflict. " + sqlException.getMessage(),
        null),
      result);
  }

  @Test
  void givenMongoConstraintViolationExceptionCauseWhenTranscodeThenOk() {
    // Given
    DataIntegrityViolationException exception = new DataIntegrityViolationException("message", new MongoDataIntegrityViolationException("mongoErrorMessage", WriteConcernResult.unacknowledged(), MongoActionOperation.INSERT));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "SEND_NOTIFICATION_CONFLICT",
        "Conflict. mongoErrorMessage",
        null),
      result);
  }
}
