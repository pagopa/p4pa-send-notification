package it.gov.pagopa.pu.send.exception.transcoder;

public interface ExceptionMessageTranscoder<T extends Exception> {
  ExceptionMessageTranscoded transcode(T exception);
}
