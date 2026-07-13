package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.model.Campaign;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CampaignService {
  Campaign createIfNotExists(String externalCampaignId, String campaignName, CreateNotificationRequest sendNotificationReq, LocalDate sendNotificationCreationDate);
  List<String> fetchAllIds();

  void alignCampaign(String campaignId);
  void incrementTotalAndUpdateEndDate(String campaignId, LocalDate endDate);
  void handleStatusChange(String campaignId, NotificationStatusChangeDTO notificationStatusChangeDTO);
  Campaign getCampaignById(String campaignId);
  void deleteCampaignById(String campaignId);
  void updateStartDate(String campaignId, LocalDate startDate);
  void updateEndDate(String campaignId, LocalDate endDate);
  PagedCampaign findCampaignsByFilters(Long organizationId, LocalDate dateFrom, LocalDate dateTo, String orgSubUnitCode, String campaignName, String externalCampaignId, Pageable pageable);
}
