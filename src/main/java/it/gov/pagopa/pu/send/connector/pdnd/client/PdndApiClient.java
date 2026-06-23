package it.gov.pagopa.pu.send.connector.pdnd.client;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.pdnd.dto.generated.PdndServiceType;
import it.gov.pagopa.pu.send.connector.pdnd.config.PagopaPdndApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find voucher token for service SEND organizationId {} and subUnitCode {} ", organizationId, subUnitCode);
      return null;
    }
  }
}
