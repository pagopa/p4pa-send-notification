package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;

public interface CampaignService {
  void createIfNotExists(String campaignId, String campaignName, String subUnitCode, SendNotification sendNotification);
}
