package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.dto.generated.PagedSendNotifications;
import it.gov.pagopa.pu.send.dto.generated.RenameCampaignRequest;
import it.gov.pagopa.pu.send.model.SendCampaign;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface CampaignService {
  SendCampaign createIfNotExists(String externalCampaignId, String campaignName, CreateNotificationRequest sendNotificationReq, LocalDate sendNotificationCreationDate);
  List<String> fetchAllIds();
  void alignCampaign(String campaignId, OffsetDateTime recalculationDate);
  void incrementTotalAndUpdateEndDate(String campaignId, LocalDate endDate);
  void handleStatusChange(String campaignId, NotificationStatusChangeDTO notificationStatusChangeDTO);
  SendCampaign getCampaignById(String campaignId);
  void deleteCampaignById(String campaignId);
  void updateStartDate(String campaignId, LocalDate startDate);
  void updateEndDate(String campaignId, LocalDate endDate);
  PagedCampaign findCampaignsByFilters(CampaignFiltersDTO campaignFiltersDTO, Pageable pageable);
  PagedSendNotifications getCampaignSendNotifications(SendNotificationFiltersDTO sendNotificationFiltersDTO, String fiscalCode, Pageable pageable);
  void renameCampaign(String campaignId, RenameCampaignRequest renameCampaignRequest);
  OffsetDateTime findLatestFullRecalculationDate();
  OffsetDateTime findFirstCampaignStartDate();
  List<String> findIdsOfUpdatedCampaignsByNotificationUpdateDate(OffsetDateTime lastRecalculationDate);
}
