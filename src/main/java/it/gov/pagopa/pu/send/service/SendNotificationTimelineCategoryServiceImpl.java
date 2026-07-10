package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.CampaignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Lazy
public class SendNotificationTimelineCategoryServiceImpl implements SendNotificationTimelineCategoryService {

  private final SendNotificationStatusHandlerService sendNotificationStatusHandlerService;

  public SendNotificationTimelineCategoryServiceImpl(
    SendNotificationStatusHandlerService sendNotificationStatusHandlerService) {
    this.sendNotificationStatusHandlerService = sendNotificationStatusHandlerService;
  }

  @Override
  public void notifySendNotificationTimelineCategory(SendNotificationNoPII notification, List<TimelineElementCategoryV27DTO> timelineElementCategories) {
    List<TimelineElementCategoryV27DTO> timelineElementCategoriesOfInterest =
      timelineElementCategories.stream()
        .filter(CampaignUtils.TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS::containsKey)
        .toList();
    if(timelineElementCategoriesOfInterest.isEmpty()) {
      return;
    }
    sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      notification.getSendNotificationId(),
      notification.getCampaignId(),
      notification.getStreamEventStatus(),
      timelineElementCategoriesOfInterest.getLast()
    );
  }

}
