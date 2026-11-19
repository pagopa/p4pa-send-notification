package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.PagedSendNotificationAndCampaignViewDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationAndCampaignViewDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class PagedSendNotificationAndCampaignViewDTOMapper {

  public PagedSendNotificationAndCampaignViewDTO map(Page<SendNotificationNoPII> sendNotificationsPage, Map<String, Campaign> campaignMap) {
    return PagedSendNotificationAndCampaignViewDTO.builder()
      .content(sendNotificationsPage.getContent().stream()
        .map(sendNotification -> map(sendNotification, campaignMap.get(sendNotification.getCampaignId())))
        .toList())
      .size((long) sendNotificationsPage.getSize())
      .totalPages((long) sendNotificationsPage.getTotalPages())
      .totalElements(sendNotificationsPage.getTotalElements())
      .number((long) sendNotificationsPage.getNumber())
      .build();
  }

  private SendNotificationAndCampaignViewDTO map(SendNotificationNoPII sendNotification, Campaign campaign) {
    if (campaign == null) {
      throw new IllegalStateException("[CAMPAIGN_NOT_FOUND] No Campaign found with campaignId %s for sendNotification with Id %s"
        .formatted(sendNotification.getCampaignId(), sendNotification.getSendNotificationId()));
    }
    return SendNotificationAndCampaignViewDTO.builder()
      .sendNotificationId(sendNotification.getSendNotificationId())
      .organizationId(sendNotification.getOrganizationId())
      .orgSubUnitCode(sendNotification.getOrgSubUnitCode())
      .campaignId(sendNotification.getCampaignId())
      .campaignName(campaign.getCampaignName())
      .externalCampaignId(campaign.getExternalId())
      .iun(sendNotification.getIun())
      .subject(sendNotification.getSubject())
      .status(sendNotification.getStatus())
      .creationDate(Optional.ofNullable(sendNotification.getCreationDate()).map(LocalDateTime::toLocalDate).orElse(null))
      .build();
  }
}
