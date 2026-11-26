package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.PagedSendNotificationAndCampaignViewDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationAndCampaignViewDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Map;

class PagedSendNotificationAndCampaignViewDTOMapperTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private final PagedSendNotificationAndCampaignViewDTOMapper mapper = new PagedSendNotificationAndCampaignViewDTOMapper();

  @Test
  void whenMapThenOk() {
    // Given
    SendNotificationNoPII notification = podamFactory.manufacturePojo(SendNotificationNoPII.class);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    Page<SendNotificationNoPII> notificationsPage = new PageImpl<>(List.of(notification), PageRequest.of(1, 1), 5L);

    // When
    PagedSendNotificationAndCampaignViewDTO result = mapper.map(notificationsPage, Map.of(notification.getCampaignId(), campaign));

    // Then
    TestUtils.checkNotNullFields(result);
    Assertions.assertEquals(1L, result.getSize());
    Assertions.assertEquals(5L, result.getTotalPages());
    Assertions.assertEquals(5L, result.getTotalElements());
    Assertions.assertEquals(1L, result.getNumber());
    Assertions.assertEquals(1, result.getContent().size());
    SendNotificationAndCampaignViewDTO item = result.getContent().getFirst();
    TestUtils.checkNotNullFields(item);
    Assertions.assertEquals(notification.getSendNotificationId(), item.getSendNotificationId());
    Assertions.assertEquals(notification.getOrganizationId(), item.getOrganizationId());
    Assertions.assertEquals(notification.getOrgSubUnitCode(), item.getOrgSubUnitCode());
    Assertions.assertEquals(notification.getCampaignId(), item.getCampaignId());
    Assertions.assertEquals(campaign.getCampaignName(), item.getCampaignName());
    Assertions.assertEquals(campaign.getExternalId(), item.getExternalCampaignId());
    Assertions.assertEquals(notification.getIun(), item.getIun());
    Assertions.assertEquals(notification.getSubject(), item.getSubject());
    Assertions.assertEquals(notification.getStatus(), item.getStatus());
    Assertions.assertEquals(notification.getCreationDate().toLocalDate(), item.getCreationDate());
  }

  @Test
  void givenMissingCampaignWhenMapThenIllegalStateException() {
    // Given
    SendNotificationNoPII notification = podamFactory.manufacturePojo(SendNotificationNoPII.class);
    Page<SendNotificationNoPII> notificationsPage = new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1L);
    Map<String, Campaign> campaignsById = Map.of();

    // When
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> mapper.map(notificationsPage, campaignsById));

    // Then
    Assertions.assertEquals("[CAMPAIGN_NOT_FOUND] No Campaign found with campaignId %s for sendNotification with Id %s"
      .formatted(notification.getCampaignId(), notification.getSendNotificationId()), exception.getMessage());
  }
}
