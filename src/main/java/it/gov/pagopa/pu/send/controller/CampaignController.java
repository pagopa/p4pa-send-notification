package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.CampaignApi;
import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.service.CampaignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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

  @Override
  public ResponseEntity<PagedCampaign> findCampaignsByFilters(Long organizationId, LocalDate dateFrom, LocalDate dateTo, String orgSubUnitCode, String campaignName, String externalCampaignId, Pageable pageable) {
    log.info("retrieve campaigns by filters having organizationId {}, dateFrom {} and dateTo {}", organizationId, dateFrom, dateTo);
    return ResponseEntity.ok(
      campaignService.findCampaignsByFilters(organizationId, dateFrom, dateTo, orgSubUnitCode, campaignName, externalCampaignId, pageable)
    );
  }

  @Override
  public ResponseEntity<Campaign> getCampaign(String campaignId) {
    log.info("retrieve campaign having campaignId {}", campaignId);
    return ResponseEntity.ok(campaignService.getCampaignById(campaignId));
  }
}
