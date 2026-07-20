package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.List;

public interface SendNotificationStreamEventService {
  void notifySendNotificationStreamEvents(SendNotificationNoPII notification, List<StreamEventSummaryDTO> streamEvents);
}
