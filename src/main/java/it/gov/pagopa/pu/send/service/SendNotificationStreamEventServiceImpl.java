package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.CampaignUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotificationStreamEventServiceImpl implements SendNotificationStreamEventService {
  private final SendNotificationStatusHandlerService sendNotificationStatusHandlerService;
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;

  @Override
  public void notifySendNotificationStreamEvents(SendNotificationNoPII notification, List<StreamEventSummaryDTO> streamEvents) {
    sendNotificationNoPIIRepository.pushStreamEventsHistory(notification.getSendNotificationId(), streamEvents);

    List<TimelineElementCategoryV27DTO> timelineElementCategoriesOfInterest =
      streamEvents.stream()
        .map(StreamEventSummaryDTO::getTimelineElementCategory)
        .filter(CampaignUtils.TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS::containsKey)
        .toList();
    if(timelineElementCategoriesOfInterest.isEmpty()) {
      return;
    }

    sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      notification.getSendNotificationId(),
      notification.getCampaignId(),
      notification.getLastEventOfInterest(),
      timelineElementCategoriesOfInterest.getLast()
    );
  }

}
