package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    Long organizationId = sendNotification.getOrganizationId();
    String orgSubUnitCode = sendNotification.getOrgSubUnitCode();

    Optional<Campaign> existingCampaign = campaignRepository
      .findByExternalCampaignIdAndOrganizationIdAndOrgSubUnitCode(externalCampaignId, organizationId, orgSubUnitCode);

    if (existingCampaign.isPresent()) {
      return existingCampaign.get();
    }

    LocalDate creationDate = sendNotification.getNoPII().getCreationDate().toLocalDate();

    Campaign newCampaign = Campaign.builder()
      .externalId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(organizationId)
      .orgSubUnitCode(orgSubUnitCode)
      .startDate(creationDate)
      .endDate(creationDate)
      .build();

    // TODO: handle counter in P4ADEV-4790

    return campaignRepository.save(newCampaign);
  }
}
