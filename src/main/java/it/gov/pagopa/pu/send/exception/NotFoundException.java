package it.gov.pagopa.pu.send.exception;

public class NotFoundException extends BaseBusinessException {

    public NotFoundException(String code, String message) {
            super(code, message);
        }
}
