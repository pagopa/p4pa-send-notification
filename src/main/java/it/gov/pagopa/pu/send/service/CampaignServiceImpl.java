package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;

  public CampaignServiceImpl(CampaignRepository campaignRepository) {
    this.campaignRepository = campaignRepository;
  }

  @Override
  public Campaign createIfNotExists(String externalCampaignId, String campaignName, SendNotification sendNotification) {
    Optional<Campaign> existingCampaign = campaignRepository.findByExternalCampaignId(externalCampaignId);
    if (existingCampaign.isPresent()) {
      return existingCampaign.get();
    }

    Campaign newCampaign = Campaign.builder()
      .externalCampaignId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(sendNotification.getOrganizationId())
      .orgSubUnitCode(sendNotification.getOrgSubUnitCode())
      .startDate(sendNotification.getNoPII().getCreationDate().toLocalDate())
      .build();

    // TODO: handle end date and counter in P4ADEV-4790

    return campaignRepository.save(newCampaign);
  }
}
