package it.gov.pagopa.pu.send.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.mapper.PagedCampaignMapper;
import it.gov.pagopa.pu.send.mapper.PagedSendNotificationsMapper;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.CampaignUtils;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;

import static it.gov.pagopa.pu.send.util.CampaignUtils.COUNTER_RULES;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
  private final CampaignRepository campaignRepository;
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final PagedCampaignMapper pagedCampaignMapper;
  private final PagedSendNotificationsMapper pagedSendNotificationsMapper;
  private final DataCipherService dataCipherService;

  public CampaignServiceImpl(CampaignRepository campaignRepository, SendNotificationNoPIIRepository sendNotificationNoPIIRepository, PagedCampaignMapper pagedCampaignMapper, PagedSendNotificationsMapper pagedSendNotificationsMapper, DataCipherService dataCipherService) {
    this.campaignRepository = campaignRepository;
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
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
  public PagedCampaign findCampaignsByFilters(Long organizationId, LocalDate dateFrom, LocalDate dateTo, String orgSubUnitCode, String campaignName, String externalCampaignId, Pageable pageable) {
    return pagedCampaignMapper.mapToPagedCampaign(
      campaignRepository.findCampaignsByFilters(organizationId, dateFrom, dateTo, orgSubUnitCode, campaignName, externalCampaignId, pageable)
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
  public List<String> calculateActiveCounters(List<StreamEventSummaryDTO> history) {
    if (history == null || history.isEmpty()) {
      return Collections.emptyList();
    }

    Set<String> candidateCounters = new HashSet<>();

    for (Map.Entry<String, CampaignUtils.CounterRule> entry : COUNTER_RULES.entrySet()) {
      String counter = entry.getKey();
      CampaignUtils.CounterRule rule = entry.getValue();

      boolean matchesActivation = rule.activationConditions().stream()
        .anyMatch(condition -> isConditionMet(condition, history));

      boolean matchesDeactivation = rule.deactivationConditions().stream()
        .anyMatch(condition -> isConditionMet(condition, history));

      if (matchesActivation && !matchesDeactivation) {
        candidateCounters.add(counter);
      }
    }

    List<String> activeCounters = new ArrayList<>();

    for (String candidate : candidateCounters) {
      CampaignUtils.CounterRule rule = COUNTER_RULES.get(candidate);

      boolean isExcludedByOtherCounter = rule.deactivatingCounters().stream()
        .anyMatch(candidateCounters::contains);

      if (!isExcludedByOtherCounter) {
        activeCounters.add(candidate);
      }
    }

    return activeCounters;
  }

  private boolean isConditionMet(StreamEventSummaryDTO condition, List<StreamEventSummaryDTO> history) {
    return history.stream().anyMatch(event -> {
      boolean statusMatches = Objects.equals(condition.getNewNotificationStatus(), event.getNewNotificationStatus());

      boolean categoryMatches = condition.getTimelineElementCategory() == null ||
        Objects.equals(condition.getTimelineElementCategory(), event.getTimelineElementCategory());

      return statusMatches && categoryMatches;
    });
  }
}
