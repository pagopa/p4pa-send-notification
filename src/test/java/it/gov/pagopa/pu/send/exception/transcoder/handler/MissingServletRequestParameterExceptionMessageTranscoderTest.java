package it.gov.pagopa.pu.send.exception.transcoder.handler;

import it.gov.pagopa.pu.send.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

class MissingServletRequestParameterExceptionMessageTranscoderTest {

  private final MissingServletRequestParameterExceptionMessageTranscoder transcoder = new MissingServletRequestParameterExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    MissingServletRequestParameterException exception = new MissingServletRequestParameterException("paramName", "paramType");

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "SEND_NOTIFICATION_BAD_REQUEST",
        exception.getMessage(),
        List.of(ErrorFieldDTO.builder()
          .field("paramName")
          .error("NotNull")
          .message(exception.getMessage())
          .build())),
      result
    );
  }
}
