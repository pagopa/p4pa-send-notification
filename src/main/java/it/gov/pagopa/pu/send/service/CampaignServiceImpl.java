package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;

  public CampaignServiceImpl(CampaignRepository campaignRepository) {
    this.campaignRepository = campaignRepository;
  }

  @Override
  public Campaign createIfNotExists(String externalCampaignId, String campaignName, CreateNotificationRequest sendNotificationReq, LocalDate sendNotificationCreationDate) {
    Long organizationId = sendNotificationReq.getOrganizationId();
    String orgSubUnitCode = sendNotificationReq.getSubUnitCode();

    Optional<Campaign> existingCampaign = campaignRepository
      .findByExternalIdAndOrganizationIdAndOrgSubUnitCode(externalCampaignId, organizationId, orgSubUnitCode);

    if (existingCampaign.isPresent()) {
      return existingCampaign.get();
    }

    Counters counters = new Counters();
    counters.setTotal(1L);

    Campaign newCampaign = Campaign.builder()
      .externalId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(organizationId)
      .orgSubUnitCode(orgSubUnitCode)
      .startDate(sendNotificationCreationDate)
      .endDate(sendNotificationCreationDate)
      .counters(counters)
      .build();

    return campaignRepository.save(newCampaign);
  }

  @Override
  public List<String> fetchAllIds() {
    List<CampaignIdView> campaigns = campaignRepository.findAllCampaignIdsBy();

    return campaigns.stream().map(CampaignIdView::getCampaignId).toList();
  }
}
