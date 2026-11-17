package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.Set;

public interface CampaignCountersService {
  Set<String> calculateActiveCounters(SendNotificationNoPII notification);
}
