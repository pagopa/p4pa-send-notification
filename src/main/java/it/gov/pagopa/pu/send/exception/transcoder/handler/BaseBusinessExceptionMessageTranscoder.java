package it.gov.pagopa.pu.send.exception.transcoder.handler;

import it.gov.pagopa.pu.send.exception.common.BaseBusinessException;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.send.exception.transcoder.ExceptionMessageTranscoder;

public class BaseBusinessExceptionMessageTranscoder implements ExceptionMessageTranscoder<BaseBusinessException> {
  @Override
  public ExceptionMessageTranscoded transcode(BaseBusinessException businessException) {
    return new ExceptionMessageTranscoded(businessException.getCode(), businessException.getMessage(), businessException.getFields());
  }
}
