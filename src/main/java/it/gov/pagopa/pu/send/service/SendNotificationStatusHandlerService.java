package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.Campaign;

import java.time.LocalDate;
import java.util.List;

public interface SendNotificationStatusHandlerService {
  Campaign handleNewSendNotification(CreateNotificationRequest createNotificationRequest);
  void handleSendNotificationStatusUpdate(String sendNotificationId, String campaignId, List<StreamEventSummaryDTO> oldHistory, List<StreamEventSummaryDTO> eventToPush);
  void handleDeletedSendNotification(String campaignId, LocalDate notificationCreationDate, TimelineElementCategoryV27DTO currentLatestEventOfInterest);
}
