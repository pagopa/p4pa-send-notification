package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.exception.common.BaseBusinessException;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class InvalidTaxonomyException extends BaseBusinessException {
  public InvalidTaxonomyException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_INVALID_TAXONOMY_CODE, message);
  }
}
