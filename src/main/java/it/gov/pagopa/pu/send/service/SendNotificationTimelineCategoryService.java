package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.List;

public interface SendNotificationTimelineCategoryService {
  void notifySendNotificationTimelineCategory(SendNotificationNoPII notification, List<TimelineElementCategoryV27DTO> timelineElementCategories);
}
