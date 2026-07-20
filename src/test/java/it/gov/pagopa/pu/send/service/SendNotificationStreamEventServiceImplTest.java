package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationStatusV26DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
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
class SendNotificationStreamEventServiceImplTest {
  @Mock
  private SendNotificationStatusHandlerService sendNotificationStatusHandlerServiceMock;
  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;

  @InjectMocks
  private SendNotificationStreamEventServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendNotificationStatusHandlerServiceMock,
      sendNotificationNoPIIRepositoryMock
    );
  }

  @Test
  void givenTimelineElementCategoriesOfInterestWhenNotifySendNotificationStreamEventsThenUpdateLastEventOfInterestAndPushHistory() {
    //GIVEN
    SendNotificationNoPII sendNotification = SendNotificationNoPII.builder()
      .sendNotificationId("sendNotificationId1")
      .campaignId("sendCampaignId1")
      .lastEventOfInterest(TimelineElementCategoryV27DTO.REQUEST_ACCEPTED)
      .build();

    List<StreamEventSummaryDTO> streamEvents =
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.REQUEST_ACCEPTED),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS)
      );

    doNothing()
      .when(sendNotificationStatusHandlerServiceMock)
      .handleSendNotificationStatusUpdate(
        sendNotification.getSendNotificationId(),
        sendNotification.getCampaignId(),
        sendNotification.getLastEventOfInterest(),
        streamEvents.getLast().getTimelineElementCategory()
      );

    doNothing()
      .when(sendNotificationNoPIIRepositoryMock)
      .pushStreamEventsHistory(sendNotification.getSendNotificationId(), streamEvents);

    //WHEN
    service.notifySendNotificationStreamEvents(sendNotification, streamEvents);

    //THEN
    verify(sendNotificationStatusHandlerServiceMock).handleSendNotificationStatusUpdate(
      sendNotification.getSendNotificationId(),
      sendNotification.getCampaignId(),
      sendNotification.getLastEventOfInterest(),
      streamEvents.getLast().getTimelineElementCategory()
    );
  }

  @Test
  void givenNoTimelineElementCategoriesOfInterestWhenNotifySendNotificationStreamEventsThenSkipUpdateLastEventOfInterestAndPushHistory() {
    //GIVEN
    SendNotificationNoPII sendNotification = SendNotificationNoPII.builder()
      .sendNotificationId("sendNotificationId1")
      .campaignId("sendCampaignId1")
      .lastEventOfInterest(TimelineElementCategoryV27DTO.REQUEST_ACCEPTED)
      .build();

    List<StreamEventSummaryDTO> streamEvents =
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.AAR_CREATION_REQUEST),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.AAR_GENERATION)
      );

    doNothing()
      .when(sendNotificationNoPIIRepositoryMock)
      .pushStreamEventsHistory(sendNotification.getSendNotificationId(), streamEvents);

    //WHEN
    service.notifySendNotificationStreamEvents(sendNotification, streamEvents);

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
