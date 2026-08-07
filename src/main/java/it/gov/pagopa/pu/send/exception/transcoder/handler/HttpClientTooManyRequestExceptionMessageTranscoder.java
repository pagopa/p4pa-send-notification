package it.gov.pagopa.pu.send.exception.transcoder.handler;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.web.client.HttpClientErrorException;

public class HttpClientTooManyRequestExceptionMessageTranscoder implements ExceptionMessageTranscoder<HttpClientErrorException.TooManyRequests> {
  @Override
  public ExceptionMessageTranscoded transcode(HttpClientErrorException.TooManyRequests tooManyRequestsException) {
    return new ExceptionMessageTranscoded(
      SendNotificationErrorDTO.CategoryEnum.SEND_NOTIFICATION_TOO_MANY_REQUESTS.getValue(),
      tooManyRequestsException.getMessage(),
      null);
  }
}
