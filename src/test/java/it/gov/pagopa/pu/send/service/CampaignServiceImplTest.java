package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.mapper.PagedCampaignMapper;
import it.gov.pagopa.pu.send.mapper.PagedSendNotificationsMapper;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import it.gov.pagopa.pu.send.repository.CampaignRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();
  @Mock
  private CampaignRepository campaignRepositoryMock;
  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private PagedCampaignMapper pagedCampaignMapperMock;
  @Mock
  private PagedSendNotificationsMapper pagedSendNotificationsMapperMock;
  @Mock
  private DataCipherService dataCipherServiceMock;

  @InjectMocks
  private CampaignServiceImpl campaignService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      campaignRepositoryMock,
      sendNotificationNoPIIRepositoryMock,
      pagedCampaignMapperMock,
      pagedSendNotificationsMapperMock,
      dataCipherServiceMock
    );
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

    assertNotNull(result);
    assertEquals(existingCampaign, result);
  }

  @Test
  void givenNotExistingCampaignWhenCreateIfNotExistsThenSaveNewCampaign() {
    String externalCampaignId = "externalCampaignId";
    String campaignName = "campaignName";
    LocalDate creationDate = LocalDate.of(2026, Month.JUNE, 18);

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setOrganizationId(1L);
    request.setSubUnitCode("orgSubUnitCode");

    Campaign expectedCampaign = Campaign.builder()
      .externalId(externalCampaignId)
      .campaignName(campaignName)
      .organizationId(request.getOrganizationId())
      .orgSubUnitCode(request.getSubUnitCode())
      .startDate(creationDate)
      .endDate(creationDate)
      .build();

    when(campaignRepositoryMock.findByExternalIdAndOrganizationIdAndOrgSubUnitCode(
      externalCampaignId, request.getOrganizationId(), request.getSubUnitCode()))
      .thenReturn(Optional.empty());

    when(campaignRepositoryMock.save(expectedCampaign)).thenReturn(expectedCampaign);

    Campaign result = campaignService.createIfNotExists(externalCampaignId, campaignName, request, creationDate);

    assertNotNull(result);
    assertEquals(expectedCampaign, result);
  }

  @Test
  void whenFetchAllIdsThenReturnListOfStringIds() {
    CampaignIdView campaignIdView = new CampaignIdView();
    campaignIdView.setCampaignId("campaignId");

    when(campaignRepositoryMock.findAllCampaignIdsByOrderByCampaignIdAsc()).thenReturn(List.of(campaignIdView));

    List<String> result = campaignService.fetchAllIds();

    assertNotNull(result);
    assertEquals(List.of("campaignId"), result);
  }

  @Test
  void givenExistingCampaignWhenAlignCampaignThenUpdateCountersAndSave() {
    String campaignId = "campaignId";
    Campaign campaign = Campaign.builder().build();
    Counters mockCounters = new Counters();

    when(campaignRepositoryMock.findById(campaignId)).thenReturn(Optional.of(campaign));
    when(sendNotificationNoPIIRepositoryMock.calculateCampaignCounters(campaignId)).thenReturn(mockCounters);
    when(campaignRepositoryMock.save(campaign)).thenReturn(campaign);

    campaignService.alignCampaign(campaignId);

    assertEquals(mockCounters, campaign.getCounters());
  }

  @Test
  void givenNotExistingCampaignWhenAlignCampaignThenThrowNotFoundException() {
    String campaignId = "campaignId";

    when(campaignRepositoryMock.findById(campaignId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(
      NotFoundException.class,
      () -> campaignService.alignCampaign(campaignId)
    );

    assertEquals(ErrorCodeConstants.ERROR_CODE_CAMPAIGN_NOT_FOUND, exception.getCode());
    assertEquals(String.format("Campaign having id %s not found", campaignId), exception.getMessage());
  }

  @Test
  void whenIncrementTotalAndUpdateEndDateThenOk() {
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    when(campaignRepositoryMock.incrementTotalAndUpdateEndDate(campaign.getCampaignId(), endDate)).thenReturn(updateResult);

    assertDoesNotThrow(()->campaignService.incrementTotalAndUpdateEndDate(campaign.getCampaignId(), endDate));
  }

  @Test
  void whenHandleStatusChangeThenOk() {
    String campaignId = "campaignId";
    NotificationStatusChangeDTO notificationStatusChangeDTO = podamFactory.manufacturePojo(NotificationStatusChangeDTO.class);
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    when(campaignRepositoryMock.updateCampaignCounters(campaignId, notificationStatusChangeDTO)).thenReturn(updateResult);

    assertDoesNotThrow(()->campaignService.handleStatusChange(campaignId, notificationStatusChangeDTO));
  }

  @Test
  void givenEmptyFieldsListWhenHandleStatusChangeThenNull() {
    String campaignId = "campaignId";
    NotificationStatusChangeDTO notificationStatusChangeDTO = new NotificationStatusChangeDTO();

   assertDoesNotThrow(()->campaignService.handleStatusChange(campaignId, notificationStatusChangeDTO));
  }

  @Test
  void givenNoNotificationStatusChangeDTOWhenHandleStatusChangeThenNull() {
    String campaignId = "campaignId";

    assertDoesNotThrow(()->campaignService.handleStatusChange(campaignId, null));
  }

  @Test
  void whenGetCampaignByIdThenOk() {
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);

    when(campaignRepositoryMock.findById(campaign.getCampaignId())).thenReturn(Optional.of(campaign));

    Campaign result = campaignService.getCampaignById(campaign.getCampaignId());

    assertNotNull(result);
    assertEquals(campaign, result);
  }

  @Test
  void givenNoCampaignWhenGetCampaignByIdThenNotFoundException() {
    String campaignId = "campaignId";
    when(campaignRepositoryMock.findById(campaignId)).thenReturn(Optional.empty());

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> campaignService.getCampaignById(campaignId));

    assertEquals(ErrorCodeConstants.ERROR_CODE_CAMPAIGN_NOT_FOUND, notFoundException.getCode());
  }

  @Test
  void whenDeleteCampaignByIdThenNull() {
    String campaignId = "campaignId";
    doNothing().when(campaignRepositoryMock).deleteById(campaignId);

    assertDoesNotThrow(()->campaignService.deleteCampaignById(campaignId));
  }

  @Test
  void whenUpdateStartDateThenNull() {
    String campaignId = "campaignId";
    LocalDate startDate = LocalDate.of(2026, Month.JUNE, 30);
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    when(campaignRepositoryMock.updateStartDate(campaignId, startDate)).thenReturn(updateResult);

    assertDoesNotThrow(()->campaignService.updateStartDate(campaignId, startDate));
  }

  @Test
  void whenUpdateEndDateThenNull() {
    String campaignId = "campaignId";
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    when(campaignRepositoryMock.updateEndDate(campaignId, endDate)).thenReturn(updateResult);

    assertDoesNotThrow(()->campaignService.updateEndDate(campaignId, endDate));
  }

  @Test
  void whenFindCampaignsByFiltersThenOk() {
    Long organizationId = 1L;
    LocalDate dateFrom = LocalDate.of(2026, Month.JULY, 1);
    LocalDate dateTo = LocalDate.of(2026, Month.JULY, 31);
    String orgSubUnitCode = "orgSubUnitCode";
    String campaignName = "campaignName";
    String externalCampaignId = "externalCampaignId";
    Pageable pageable = PageRequest.of(0, 10);

    List<Campaign> campaigns = podamFactory.manufacturePojo(List.class, Campaign.class);
    Page<Campaign> campaignPage = new PageImpl<>(campaigns);
    PagedCampaign expected = podamFactory.manufacturePojo(PagedCampaign.class);

    when(campaignRepositoryMock.findCampaignsByFilters(organizationId, dateFrom, dateTo, orgSubUnitCode, campaignName, externalCampaignId, pageable))
      .thenReturn(campaignPage);
    when(pagedCampaignMapperMock.mapToPagedCampaign(campaignPage)).thenReturn(expected);

    PagedCampaign result = campaignService.findCampaignsByFilters(organizationId, dateFrom, dateTo, orgSubUnitCode, campaignName, externalCampaignId, pageable);

    assertEquals(expected, result);
  }

  @Test
  void whenGetCampaignSendNotificationsThenOk(){
    SendNotificationFiltersDTO filters = podamFactory.manufacturePojo(SendNotificationFiltersDTO.class);
    String fiscalCode = "fiscalCode";
    Pageable pageable = PageRequest.of(0, 10);
    List<SendNotificationNoPII> notifications = podamFactory.manufacturePojo(List.class, SendNotificationNoPII.class);
    Page<SendNotificationNoPII> notificationPage = new PageImpl<>(notifications);
    PagedSendNotifications expectedResult = podamFactory.manufacturePojo(PagedSendNotifications.class);

    when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(filters.getFiscalCodeHash());
    when(sendNotificationNoPIIRepositoryMock.findSendNotificationsByFilters(filters,pageable)).thenReturn(notificationPage);
    when(pagedSendNotificationsMapperMock.mapToPagedSendNotifications(notificationPage)).thenReturn(expectedResult);

    PagedSendNotifications result = campaignService.getCampaignSendNotifications(filters, fiscalCode, pageable);

    assertNotNull(result);
    assertEquals(expectedResult,result);
  }

  @Test
  void givenNoFiscalCodewhenGetCampaignSendNotificationsThenOk(){
    SendNotificationFiltersDTO filters = podamFactory.manufacturePojo(SendNotificationFiltersDTO.class);
    Pageable pageable = PageRequest.of(0, 10);
    List<SendNotificationNoPII> notifications = podamFactory.manufacturePojo(List.class, SendNotificationNoPII.class);
    Page<SendNotificationNoPII> notificationPage = new PageImpl<>(notifications);
    PagedSendNotifications expectedResult = podamFactory.manufacturePojo(PagedSendNotifications.class);

    when(sendNotificationNoPIIRepositoryMock.findSendNotificationsByFilters(filters,pageable)).thenReturn(notificationPage);
    when(pagedSendNotificationsMapperMock.mapToPagedSendNotifications(notificationPage)).thenReturn(expectedResult);

    PagedSendNotifications result = campaignService.getCampaignSendNotifications(filters, null, pageable);

    assertNotNull(result);
    assertEquals(expectedResult,result);
  }

  @Test
  void whenRenameCampaignNameThenOk() {
    String campaignId = "campaignId";
    RenameCampaignRequest request = new RenameCampaignRequest();
    request.setName("NAME");

    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    when(campaignRepositoryMock.updateCampaignName(campaignId, request.getName())).thenReturn(updateResult);

    assertDoesNotThrow(()->campaignService.renameCampaign(campaignId, request));
  }
}
