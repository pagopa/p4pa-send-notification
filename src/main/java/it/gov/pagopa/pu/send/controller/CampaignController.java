package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.CampaignApi;
import it.gov.pagopa.pu.send.service.CampaignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class CampaignController implements CampaignApi {
  private final CampaignService campaignService;

  public CampaignController(CampaignService campaignService) {
    this.campaignService = campaignService;
  }

  @Override
  public ResponseEntity<List<String>> fetchAllCampaignIds() {
    log.info("retrieve all campaign ids");

    return new ResponseEntity<>(campaignService.fetchAllIds(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> alignCampaign(String campaignId) {
    log.info("align campaign with id {}", campaignId);

    campaignService.alignCampaign(campaignId);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
