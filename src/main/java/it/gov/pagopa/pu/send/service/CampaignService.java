package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.Campaign;

public interface CampaignService {
  Campaign createIfNotExists(String externalCampaignId, String campaignName, SendNotification sendNotification);
}
