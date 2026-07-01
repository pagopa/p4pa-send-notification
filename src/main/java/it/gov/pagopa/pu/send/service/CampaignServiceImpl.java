package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;

  public CampaignServiceImpl(CampaignRepository campaignRepository, SendNotificationNoPIIRepository sendNotificationNoPIIRepository) {
    this.campaignRepository = campaignRepository;
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
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

    Campaign newCampaign = Campaign.builder()
      .externalId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(organizationId)
      .orgSubUnitCode(orgSubUnitCode)
      .startDate(sendNotificationCreationDate)
      .endDate(sendNotificationCreationDate)
      .build();

    return campaignRepository.save(newCampaign);
  }

  @Override
  public List<String> fetchAllIds() {
    List<CampaignIdView> campaigns = campaignRepository.findAllCampaignIdsBy();

    return campaigns.stream().map(CampaignIdView::getCampaignId).toList();
  }

  @Override
  public void alignCampaign(String campaignId) {
    Campaign campaign = campaignRepository.findById(campaignId)
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_CAMPAIGN_NOT_FOUND,
        String.format("Campaign having id %s not found", campaignId)
      ));

    Counters counters = sendNotificationNoPIIRepository.calculateCampaignCounters(campaignId);

    campaign.setCounters(counters);

    campaignRepository.save(campaign);
  }

  @Override
  public void incrementTotalAndUpdateEndDate(String campaignId, LocalDate endDate) {
    campaignRepository.incrementTotalAndUpdateEndDate(campaignId, endDate);
  }

  @Override
  public void handleStatusChange(String campaignId, NotificationStatusChangeDTO notificationStatusChangeDTO) {
    if(notificationStatusChangeDTO==null
      || (CollectionUtils.isEmpty(notificationStatusChangeDTO.getIncrFields())
      && CollectionUtils.isEmpty(notificationStatusChangeDTO.getDecrFields()))){
      return;
    }
    log.info("Updating counters {} for campaign having id {}", notificationStatusChangeDTO, campaignId);
    campaignRepository.updateCampaignCounters(campaignId, notificationStatusChangeDTO);
  }

  @Override
  public Campaign getCampaignById(String campaignId) {
    return campaignRepository.findById(campaignId)
      .orElseThrow(()-> new NotFoundException(ErrorCodeConstants.ERROR_CODE_CAMPAIGN_NOT_FOUND, "Campaign having id "+campaignId+" not found"));
  }

  @Override
  public void deleteCampaignById(String campaignId) {
    campaignRepository.deleteById(campaignId);
  }

  @Override
  public void updateEndDate(String campaignId, LocalDate endDate) {
    campaignRepository.updateEndDate(campaignId, endDate);
  }

  @Override
  public void updateStartDate(String campaignId, LocalDate startDate) {
    campaignRepository.updateStartDate(campaignId, startDate);
  }
}
