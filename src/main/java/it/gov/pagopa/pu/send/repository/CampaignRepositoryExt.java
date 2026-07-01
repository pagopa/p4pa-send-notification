package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;

import java.time.LocalDate;

public interface CampaignRepositoryExt {
  UpdateResult incrementTotalAndUpdateEndDate(String campaignId, LocalDate endDate);
  UpdateResult updateCampaignCounters(String campaignId, NotificationStatusChangeDTO notificationStatusChangeDTO);
  UpdateResult updateStartDate(String campaignId, LocalDate startDate);
  UpdateResult updateEndDate(String campaignId, LocalDate endDate);
}
