package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.send.util.Constants.ZONEID;

@Service
@Slf4j
public class SendNotificationStatusHandlerServiceImpl implements SendNotificationStatusHandlerService {
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final CampaignService campaignService;

  public SendNotificationStatusHandlerServiceImpl(SendNotificationNoPIIRepository sendNotificationNoPIIRepository, CampaignService campaignService) {
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.campaignService = campaignService;
  }

  @Transactional
  @Override
  public Campaign handleNewSendNotification(CreateNotificationRequest createNotificationRequest) {
    LocalDate creationDate = LocalDate.now(ZONEID);
    Campaign campaign = campaignService.createIfNotExists(
      createNotificationRequest.getExternalCampaignId(),
      createNotificationRequest.getCampaignName(),
      createNotificationRequest,
      creationDate
    );

    campaignService.incrementTotalAndUpdateEndDate(campaign.getCampaignId(), creationDate);
    return campaign;
  }

  @Transactional
  @Override
  public void handleSendNotificationStatusUpdate(String sendNotificationId, String campaignId, List<StreamEventSummaryDTO> oldHistory, List<StreamEventSummaryDTO> eventToPush) {
    List<StreamEventSummaryDTO> newHistory = sendNotificationNoPIIRepository.pushStreamEventsHistory(sendNotificationId, eventToPush);
    NotificationStatusChangeDTO notificationStatusChangeDTO = handleHistoryChange(oldHistory, newHistory);
    campaignService.handleStatusChange(campaignId, notificationStatusChangeDTO);
  }

  private NotificationStatusChangeDTO handleHistoryChange(List<StreamEventSummaryDTO> oldHistory, List<StreamEventSummaryDTO> newHistory) {
    Set<String> oldCounters = campaignService.calculateActiveCounters(oldHistory);
    Set<String> newCounters = campaignService.calculateActiveCounters(newHistory);

    return NotificationStatusChangeDTO.builder()
      .incrFields(newCounters.stream().filter(c -> !oldCounters.contains(c)).collect(Collectors.toSet()))
      .decrFields(oldCounters.stream().filter(c -> !newCounters.contains(c)).collect(Collectors.toSet()))
      .build();
  }

  @Transactional
  @Override
  public void handleDeletedSendNotification(String campaignId, LocalDate notificationCreationDate, List<StreamEventSummaryDTO> history) {
    Campaign campaign = campaignService.getCampaignById(campaignId);
    if(campaign.getCounters()!=null && Objects.equals(campaign.getCounters().getTotal(),1L)){
      log.info("Deleting campaign having id {}", campaignId);
      campaignService.deleteCampaignById(campaignId);
      return;
    }

    Set<String> activeCounters = history == null
      ? new HashSet<>()
      : campaignService.calculateActiveCounters(history);

    activeCounters.add(Counters.Fields.total);

    campaignService.handleStatusChange(campaignId, NotificationStatusChangeDTO.builder()
      .decrFields(activeCounters)
      .build());

    if(campaign.getStartDate().equals(notificationCreationDate)){
      SendNotificationNoPII sendNotification = sendNotificationNoPIIRepository.findTopByCampaignIdOrderByCreationDateAsc(campaignId);
      log.info("Updating startDate of campaign having id {} to {}", campaignId, sendNotification.getCreationDate().toLocalDate());
      campaignService.updateStartDate(campaignId, sendNotification.getCreationDate().toLocalDate());
    }else if(campaign.getEndDate().equals(notificationCreationDate)){
      SendNotificationNoPII sendNotification = sendNotificationNoPIIRepository.findTopByCampaignIdOrderByCreationDateDesc(campaignId);
      log.info("Updating endDate of campaign having id {} to {}", campaignId, sendNotification.getCreationDate().toLocalDate());
      campaignService.updateEndDate(campaignId, sendNotification.getCreationDate().toLocalDate());
    }
  }
}
