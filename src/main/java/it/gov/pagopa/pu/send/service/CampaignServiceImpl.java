package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;

  public CampaignServiceImpl(CampaignRepository campaignRepository) {
    this.campaignRepository = campaignRepository;
  }

  @Override
  public void createIfNotExists(String campaignId, String campaignName, String subUnitCode, SendNotification sendNotification) {
    if (campaignRepository.findByExternalId(campaignId).isPresent()) {
      return;
    }

    Campaign newCampaign = Campaign.builder()
      .externalId(campaignId)
      .campaignName(campaignName)
      .organizationId(sendNotification.getOrganizationId())
      .orgSubUnitCode(subUnitCode)
      .startDate(sendNotification.getNoPII().getCreationDate().toLocalDate())
      .build();

    // TODO: handle end date and counter in P4ADEV-4790

    campaignRepository.save(newCampaign);
  }
}
