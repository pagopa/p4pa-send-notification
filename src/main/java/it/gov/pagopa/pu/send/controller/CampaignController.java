package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.CampaignApi;
import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.dto.generated.PagedSendNotifications;
import it.gov.pagopa.pu.send.dto.generated.RenameCampaignRequest;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.service.CampaignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
  public ResponseEntity<PagedCampaign> findCampaignsByFilters(Long organizationId, LocalDate dateFrom, LocalDate dateTo, List<String> orgSubUnitCodes, String campaignName, String externalCampaignId, Boolean fetchAll, Pageable pageable) {
    log.info("retrieve campaigns by filters having organizationId {}, dateFrom {} and dateTo {}", organizationId, dateFrom, dateTo);
    return ResponseEntity.ok(
      campaignService.findCampaignsByFilters(
        CampaignFiltersDTO.builder()
          .organizationId(organizationId)
          .dateFrom(dateFrom)
          .dateTo(dateTo)
          .orgSubUnitCodes(orgSubUnitCodes)
          .campaignName(campaignName)
          .externalCampaignId(externalCampaignId)
          .fetchAll(fetchAll)
        .build(), pageable)
    );
  }

  @Override
  public ResponseEntity<Campaign> getCampaign(String campaignId) {
    log.info("retrieve campaign having campaignId {}", campaignId);
    return ResponseEntity.ok(campaignService.getCampaignById(campaignId));
  }

  @Override
  public ResponseEntity<Void> renameCampaign(String campaignId, RenameCampaignRequest renameCampaignRequest) {
    log.info("rename campaign request having campaignId {}", campaignId);
    campaignService.renameCampaign(campaignId, renameCampaignRequest);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<PagedSendNotifications> findCampaignSendNotifications(String campaignId, Long organizationId, String iun, OffsetDateTime dateFrom, OffsetDateTime dateTo, List<NotificationStatus> statuses, String fiscalCode, Pageable pageable) {
    log.info("retrieve send notifications for campaign having campaignId {} and organizationId {}", campaignId, organizationId);
    return ResponseEntity.ok(campaignService.getCampaignSendNotifications(
      SendNotificationFiltersDTO.builder()
        .campaignId(campaignId)
        .organizationId(organizationId)
        .iun(iun)
        .dateFrom(dateFrom)
        .dateTo(dateTo)
        .statuses(statuses)
        .build(),
      fiscalCode,pageable));
  }
}
