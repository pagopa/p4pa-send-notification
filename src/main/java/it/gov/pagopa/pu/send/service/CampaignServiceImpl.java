package it.gov.pagopa.pu.send.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.dto.generated.PagedSendNotifications;
import it.gov.pagopa.pu.send.dto.generated.RenameCampaignRequest;
import it.gov.pagopa.pu.send.exception.common.NotFoundException;
import it.gov.pagopa.pu.send.mapper.PagedCampaignMapper;
import it.gov.pagopa.pu.send.mapper.PagedSendNotificationsMapper;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepositoryExtImpl;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final SendNotificationNoPIIRepositoryExtImpl sendNotificationNoPIIRepositoryExt;
  private final PagedCampaignMapper pagedCampaignMapper;
  private final PagedSendNotificationsMapper pagedSendNotificationsMapper;
  private final DataCipherService dataCipherService;

  public CampaignServiceImpl(
    CampaignRepository campaignRepository,
    SendNotificationNoPIIRepository sendNotificationNoPIIRepository,
    SendNotificationNoPIIRepositoryExtImpl sendNotificationNoPIIRepositoryExt,
    PagedCampaignMapper pagedCampaignMapper,
    PagedSendNotificationsMapper pagedSendNotificationsMapper,
    DataCipherService dataCipherService) {
    this.campaignRepository = campaignRepository;
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.sendNotificationNoPIIRepositoryExt = sendNotificationNoPIIRepositoryExt;
    this.pagedCampaignMapper = pagedCampaignMapper;
    this.pagedSendNotificationsMapper = pagedSendNotificationsMapper;
    this.dataCipherService = dataCipherService;
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
    List<CampaignIdView> campaigns = campaignRepository.findAllCampaignIdsByOrderByCampaignIdAsc();

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

  @Override
  public PagedCampaign findCampaignsByFilters(CampaignFiltersDTO campaignFiltersDTO, Pageable pageable) {
    return pagedCampaignMapper.mapToPagedCampaign(
      campaignRepository.findCampaignsByFilters(campaignFiltersDTO, pageable)
    );
  }

  @Override
  public PagedSendNotifications getCampaignSendNotifications(SendNotificationFiltersDTO sendNotificationFiltersDTO, String fiscalCode, Pageable pageable) {
    if(StringUtils.isNotBlank(fiscalCode)){
      sendNotificationFiltersDTO.setFiscalCodeHash(dataCipherService.hash(fiscalCode));
    }
    return pagedSendNotificationsMapper.mapToPagedSendNotifications(
      sendNotificationNoPIIRepository.findSendNotificationsByFilters(sendNotificationFiltersDTO, pageable)
    );
  }

  @Override
  public void renameCampaign(String campaignId, RenameCampaignRequest renameCampaignRequest) {
    campaignRepository.updateCampaignName(campaignId, renameCampaignRequest.getName());
  }

  @Override
  public OffsetDateTime findLatestFullRecalculationDate() {
    return campaignRepository.findLatestFullRecalculationDate();
  }

  @Override
  public List<String> findIdsOfUpdatedCampaignsByNotificationUpdateDate(OffsetDateTime lastRecalculationDate) {
    return sendNotificationNoPIIRepositoryExt.findIdsOfUpdatedCampaignsByNotificationUpdateDate(lastRecalculationDate);
  }
}
