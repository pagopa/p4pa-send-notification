package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.Campaign;

import java.time.LocalDate;
import java.util.List;

public interface CampaignService {
  Campaign createIfNotExists(String externalCampaignId, String campaignName, CreateNotificationRequest sendNotificationReq, LocalDate sendNotificationCreationDate);

  List<String> fetchAllIds();

  void alignCampaign(String campaignId);
}
