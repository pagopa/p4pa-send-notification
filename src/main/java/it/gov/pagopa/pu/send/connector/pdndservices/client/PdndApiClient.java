package it.gov.pagopa.pu.send.connector.pdndservices.client;

import it.gov.pagopa.pu.pdndservices.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.pdndservices.dto.generated.PdndServiceType;
import it.gov.pagopa.pu.send.connector.pdndservices.config.PagopaPdndApisHolder;
import it.gov.pagopa.pu.send.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PdndApiClient {

  private final PagopaPdndApisHolder pdndApisHolder;

  public PdndApiClient(PagopaPdndApisHolder pdndApisHolder) {
    this.pdndApisHolder = pdndApisHolder;
  }

  public PdndAuthData getVoucherToken(String accessToken, Long organizationId, String subUnitCode) {
    try{
      return pdndApisHolder.getP4paPdndApiByApiKey(accessToken)
        .getVoucherToken(PdndServiceType.SEND, organizationId, subUnitCode);
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find voucher token for service SEND organizationId {} and subUnitCode {} ", organizationId, subUnitCode);
      return null;
    }
  }
}
