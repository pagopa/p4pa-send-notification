package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
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
    LocalDate creationDate = LocalDate.of(2026, Month.JUNE, 18);

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setOrganizationId(1L);
    request.setSubUnitCode("orgSubUnitCode");

    Campaign existingCampaign = Campaign.builder().externalId(externalCampaignId).build();

    when(campaignRepositoryMock.findByExternalIdAndOrganizationIdAndOrgSubUnitCode(
      externalCampaignId, request.getOrganizationId(), request.getSubUnitCode()))
      .thenReturn(Optional.of(existingCampaign));

    Campaign result = campaignService.createIfNotExists(externalCampaignId, campaignName, request, creationDate);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(existingCampaign, result);
  }

  @Test
  void givenNotExistingCampaignWhenCreateIfNotExistsThenSaveNewCampaign() {
    String externalCampaignId = "externalCampaignId";
    String campaignName = "campaignName";
    LocalDate creationDate = LocalDate.of(2026, Month.JUNE, 18);

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setOrganizationId(1L);
    request.setSubUnitCode("orgSubUnitCode");

    Counters counters = new Counters();
    counters.setTotal(1L);

    Campaign expectedCampaign = Campaign.builder()
      .externalId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(request.getOrganizationId())
      .orgSubUnitCode(request.getSubUnitCode())
      .startDate(creationDate)
      .endDate(creationDate)
      .counters(counters)
      .build();

    when(campaignRepositoryMock.findByExternalIdAndOrganizationIdAndOrgSubUnitCode(
      externalCampaignId, request.getOrganizationId(), request.getSubUnitCode()))
      .thenReturn(Optional.empty());

    when(campaignRepositoryMock.save(expectedCampaign)).thenReturn(expectedCampaign);

    Campaign result = campaignService.createIfNotExists(externalCampaignId, campaignName, request, creationDate);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedCampaign, result);
  }

  @Test
  void whenFetchAllIdsThenReturnListOfStringIds() {
    CampaignIdView campaignIdView = new CampaignIdView();
    campaignIdView.setCampaignId("campaignId");

    when(campaignRepositoryMock.findAllCampaignIdsBy()).thenReturn(List.of(campaignIdView));

    List<String> result = campaignService.fetchAllIds();

    Assertions.assertNotNull(result);
    Assertions.assertEquals(List.of("campaignId"), result);
  }
}
