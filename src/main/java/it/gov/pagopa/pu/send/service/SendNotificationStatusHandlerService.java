package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.List;

public interface SendNotificationStatusHandlerService {
  Campaign handleNewSendNotification(CreateNotificationRequest createNotificationRequest);
  void handleSendNotificationStatusUpdate(SendNotificationNoPII notification, List<StreamEventSummaryDTO> eventsToPush);
  void handleDeletedSendNotification(SendNotificationNoPII notification);
}
