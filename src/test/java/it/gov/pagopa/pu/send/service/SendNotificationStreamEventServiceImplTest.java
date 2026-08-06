package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationStatusV26DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamEventServiceImplTest {
  @Mock
  private SendNotificationStatusHandlerService sendNotificationStatusHandlerServiceMock;

  @InjectMocks
  private SendNotificationStreamEventServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendNotificationStatusHandlerServiceMock
    );
  }

  @Test
  void givenNotificationWithHistoryWhenNotifySendNotificationStreamEventsThenInvokeStatusHandlerWithHistory() {
    // GIVEN
    List<StreamEventSummaryDTO> history = List.of(
      new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.REQUEST_ACCEPTED)
    );

    SendNotificationNoPII sendNotification = SendNotificationNoPII.builder()
      .sendNotificationId("sendNotificationId1")
      .campaignId("sendCampaignId1")
      .history(history)
      .build();

    List<StreamEventSummaryDTO> streamEvents = List.of(
      new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS)
    );

    // WHEN
    service.notifySendNotificationStreamEvents(sendNotification, streamEvents);

    // THEN
    verify(sendNotificationStatusHandlerServiceMock).handleSendNotificationStatusUpdate(
      sendNotification.getSendNotificationId(),
      sendNotification.getCampaignId(),
      history,
      streamEvents
    );
  }

  @Test
  void givenNotificationWithNullHistoryWhenNotifySendNotificationStreamEventsThenInvokeStatusHandlerWithEmptyList() {
    // GIVEN
    SendNotificationNoPII sendNotification = SendNotificationNoPII.builder()
      .sendNotificationId("sendNotificationId1")
      .campaignId("sendCampaignId1")
      .history(null)
      .build();

    List<StreamEventSummaryDTO> streamEvents = List.of(
      new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, TimelineElementCategoryV27DTO.AAR_CREATION_REQUEST)
    );

    // WHEN
    service.notifySendNotificationStreamEvents(sendNotification, streamEvents);

    // THEN
    verify(sendNotificationStatusHandlerServiceMock).handleSendNotificationStatusUpdate(
      sendNotification.getSendNotificationId(),
      sendNotification.getCampaignId(),
      Collections.emptyList(),
      streamEvents
    );
  }
}
