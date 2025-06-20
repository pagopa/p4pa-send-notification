package it.gov.pagopa.pu.send.connector.pdnd.client;

import it.gov.pagopa.pu.pdnd.dto.generated.PdndServicesEnum;
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

  public String getVoucherToken(String accessToken) {
    try{
      return pdndApisHolder.getP4paPdndApiByApiKey(accessToken)
        .getVoucherToken(PdndServicesEnum.SEND).getAccessToken();
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find voucher token for SEND service");
      return null;
    }
  }
}
