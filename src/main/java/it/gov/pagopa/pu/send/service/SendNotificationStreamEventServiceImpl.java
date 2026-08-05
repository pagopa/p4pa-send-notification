package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotificationStreamEventServiceImpl implements SendNotificationStreamEventService {
  private final SendNotificationStatusHandlerService sendNotificationStatusHandlerService;

  @Override
  public void notifySendNotificationStreamEvents(SendNotificationNoPII notification, List<StreamEventSummaryDTO> streamEvents) {
    List<StreamEventSummaryDTO> history = notification.getHistory();

    sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      notification.getSendNotificationId(),
      notification.getCampaignId(),
      history != null ? history : Collections.emptyList(),
      streamEvents
    );
  }
}
