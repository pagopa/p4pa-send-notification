package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface CampaignRepositoryExt {
  UpdateResult incrementTotalAndUpdateEndDate(String campaignId, LocalDate endDate);
  UpdateResult updateCampaignCounters(String campaignId, NotificationStatusChangeDTO notificationStatusChangeDTO);
  UpdateResult updateStartDate(String campaignId, LocalDate startDate);
  UpdateResult updateEndDate(String campaignId, LocalDate endDate);
  Page<Campaign> findCampaignsByFilters(CampaignFiltersDTO campaignFiltersDTO, Pageable pageable);
  UpdateResult updateCampaignName(String campaignId, String name);
  OffsetDateTime findLatestFullRecalculationDate();
  OffsetDateTime findFirstCampaignStartDate();
}
