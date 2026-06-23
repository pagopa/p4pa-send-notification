package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import org.junit.jupiter.api.AfterEach;
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
    String campaignId = "campaignId";
    String campaignName = "campaignName";
    String subUnitCode = "subUnitCode";
    SendNotification sendNotification = new SendNotification();

    Campaign existingCampaign = Campaign.builder().externalId(campaignId).build();

    when(campaignRepositoryMock.findByExternalId(campaignId)).thenReturn(Optional.of(existingCampaign));

    campaignService.createIfNotExists(campaignId, campaignName, subUnitCode, sendNotification);

    verify(campaignRepositoryMock, times(0)).save(any(Campaign.class));
  }

  @Test
  void givenNotExistingCampaignWhenCreateIfNotExistsThenSaveNewCampaign() {
    String campaignId = "campaignId";
    String campaignName = "campaignName";
    String subUnitCode = "subUnitCode";

    SendNotificationNoPII sendNotificationNoPII = new SendNotificationNoPII();
    sendNotificationNoPII.setCreationDate(LocalDateTime.of(2026, Month.JUNE, 18, 12, 0));

    SendNotification sendNotification = new SendNotification();
    sendNotification.setOrganizationId(1L);
    sendNotification.setNoPII(sendNotificationNoPII);

    when(campaignRepositoryMock.findByExternalId(campaignId)).thenReturn(Optional.empty());

    Campaign expectedCampaign = Campaign.builder()
      .externalId(campaignId)
      .campaignName(campaignName)
      .organizationId(sendNotification.getOrganizationId())
      .orgSubUnitCode(subUnitCode)
      .startDate(sendNotification.getNoPII().getCreationDate().toLocalDate())
      .build();

    campaignService.createIfNotExists(campaignId, campaignName, subUnitCode, sendNotification);

    verify(campaignRepositoryMock).save(expectedCampaign);
  }
}
