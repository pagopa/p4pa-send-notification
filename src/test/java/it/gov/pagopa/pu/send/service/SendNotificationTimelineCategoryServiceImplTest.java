package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationTimelineCategoryServiceImplTest {

  @Mock
  private SendNotificationStatusHandlerService sendNotificationStatusHandlerServiceMock;

  @InjectMocks
  private SendNotificationTimelineCategoryServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendNotificationStatusHandlerServiceMock
    );
  }

  @Test
  void givenTimelineElementCategoriesOfInterestWhenNotifySendNotificationTimelineCategoryThenUpdate() {
    //GIVEN
    SendNotificationNoPII sendNotification = SendNotificationNoPII.builder()
      .sendNotificationId("sendNotificationId1")
      .campaignId("sendCampaignId1")
      .lastEventOfInterest(TimelineElementCategoryV27DTO.REQUEST_ACCEPTED)
      .build();
    List<TimelineElementCategoryV27DTO> timelineElementCategories =
      List.of(
        TimelineElementCategoryV27DTO.REQUEST_ACCEPTED,
        TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS
      );
    doNothing()
      .when(sendNotificationStatusHandlerServiceMock)
      .handleSendNotificationStatusUpdate(
        sendNotification.getSendNotificationId(),
        sendNotification.getCampaignId(),
        sendNotification.getLastEventOfInterest(),
        timelineElementCategories.getLast()
      );

    //WHEN
    service.notifySendNotificationTimelineCategory(sendNotification, timelineElementCategories);

    //THEN
    verify(sendNotificationStatusHandlerServiceMock).handleSendNotificationStatusUpdate(
      sendNotification.getSendNotificationId(),
      sendNotification.getCampaignId(),
      sendNotification.getLastEventOfInterest(),
      timelineElementCategories.getLast()
    );
  }

  @Test
  void givenNoTimelineElementCategoriesOfInterestWhenNotifySendNotificationTimelineCategoryThenSkip() {
    //GIVEN
    SendNotificationNoPII sendNotification = SendNotificationNoPII.builder()
      .sendNotificationId("sendNotificationId1")
      .campaignId("sendCampaignId1")
      .lastEventOfInterest(TimelineElementCategoryV27DTO.REQUEST_ACCEPTED)
      .build();
    List<TimelineElementCategoryV27DTO> timelineElementCategories =
      List.of(
        TimelineElementCategoryV27DTO.AAR_CREATION_REQUEST,
        TimelineElementCategoryV27DTO.AAR_GENERATION
      );

    //WHEN
    service.notifySendNotificationTimelineCategory(sendNotification, timelineElementCategories);

    //THEN
    verify(sendNotificationStatusHandlerServiceMock, times(0))
      .handleSendNotificationStatusUpdate(
        eq(sendNotification.getSendNotificationId()),
        eq(sendNotification.getCampaignId()),
        eq(sendNotification.getLastEventOfInterest()),
        isA(TimelineElementCategoryV27DTO.class)
      );
  }
}
