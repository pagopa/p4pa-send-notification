package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {
  @Mock
  private CampaignRepository campaignRepositoryMock;

  @InjectMocks
  private CampaignServiceImpl campaignService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(campaignRepositoryMock);
  }

  @Test
  void givenExistingCampaignWhenCreateIfNotExistsThenDoNothing() {
    String externalCampaignId = "externalCampaignId";
    String campaignName = "campaignName";
    SendNotification sendNotification = new SendNotification();

    Campaign existingCampaign = Campaign.builder().externalCampaignId(externalCampaignId).build();

    when(campaignRepositoryMock.findByExternalCampaignId(externalCampaignId)).thenReturn(Optional.of(existingCampaign));

    Campaign result = campaignService.createIfNotExists(externalCampaignId, campaignName, sendNotification);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(existingCampaign, result);
  }

  @Test
  void givenNotExistingCampaignWhenCreateIfNotExistsThenSaveNewCampaign() {
    String externalCampaignId = "externalCampaignId";
    String campaignName = "campaignName";

    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();
    sendNotificationNoPII.setCreationDate(LocalDateTime.of(2026, Month.JUNE, 18, 12, 0));

    SendNotification sendNotification = new SendNotification();
    sendNotification.setOrganizationId(1L);
    sendNotification.setNoPII(sendNotificationNoPII);

    Campaign expectedCampaign = Campaign.builder()
      .externalCampaignId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(sendNotification.getOrganizationId())
      .orgSubUnitCode(sendNotification.getOrgSubUnitCode())
      .startDate(sendNotification.getNoPII().getCreationDate().toLocalDate())
      .build();

    when(campaignRepositoryMock.findByExternalCampaignId(externalCampaignId)).thenReturn(Optional.empty());
    when(campaignRepositoryMock.save(expectedCampaign)).thenReturn(expectedCampaign);

    Campaign result = campaignService.createIfNotExists(externalCampaignId, campaignName, sendNotification);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedCampaign, result);
  }
}
