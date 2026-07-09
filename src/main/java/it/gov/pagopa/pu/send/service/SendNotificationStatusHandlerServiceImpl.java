package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.CampaignUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
  public void handleSendNotificationStatusUpdate(String sendNotificationId, String campaignId, TimelineElementCategoryV27DTO oldStatus, TimelineElementCategoryV27DTO newStatus) {
    if(Objects.equals(oldStatus,newStatus)){
      return;
    }
    NotificationStatusChangeDTO notificationStatusChangeDTO = handleStatusChange(oldStatus, newStatus);
    campaignService.handleStatusChange(campaignId, notificationStatusChangeDTO);
    sendNotificationNoPIIRepository.updateStreamEventStatusById(sendNotificationId, newStatus);
  }

  private NotificationStatusChangeDTO handleStatusChange(TimelineElementCategoryV27DTO oldStatus, TimelineElementCategoryV27DTO newStatus){
    Set<String> oldCounters = (oldStatus != null) ? CampaignUtils.TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS.get(oldStatus) : Set.of();
    Set<String> newCounters = (newStatus != null) ? CampaignUtils.TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS.get(newStatus) : Set.of();
    return NotificationStatusChangeDTO.builder()
      .incrFields(newCounters.stream().filter(c -> !oldCounters.contains(c)).collect(Collectors.toSet()))
      .decrFields(oldCounters.stream().filter(c -> !newCounters.contains(c)).collect(Collectors.toSet()))
      .build();
  }

  @Transactional
  @Override
  public void handleDeletedSendNotification(String campaignId, LocalDate notificationCreationDate, TimelineElementCategoryV27DTO currentStreamEventStatus) {
    Campaign campaign = campaignService.getCampaignById(campaignId);
    if(campaign.getCounters()!=null && Objects.equals(campaign.getCounters().getTotal(),1L)){
      log.info("Deleting campaign having id {}", campaignId);
      campaignService.deleteCampaignById(campaignId);
      return;
    }

    Set<String> activeCounters;
    if(currentStreamEventStatus==null){
      activeCounters = new HashSet<>();
    }else {
      activeCounters = new HashSet<>(
        CampaignUtils.TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS.getOrDefault(currentStreamEventStatus, new HashSet<>())
      );
    }
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
