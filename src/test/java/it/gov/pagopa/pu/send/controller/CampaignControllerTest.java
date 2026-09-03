package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.dto.generated.PagedSendNotifications;
import it.gov.pagopa.pu.send.dto.generated.RenameCampaignRequest;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.service.CampaignService;
import it.gov.pagopa.pu.send.util.Constants;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignControllerTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private CampaignService campaignServiceMock;

  @InjectMocks
  private  CampaignController campaignController;

  @AfterEach
  void verifyNoMoreInteraction(){
    Mockito.verifyNoMoreInteractions(
      campaignServiceMock
    );
  }

  @Test
  void whenFetchAllCampaignIdsThenOk() {
    List<String> expectedIds = List.of("id1", "id2", "id3");

    when(campaignServiceMock.fetchAllIds()).thenReturn(expectedIds);

    ResponseEntity<List<String>> response = campaignController.fetchAllCampaignIds();

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedIds, response.getBody());
  }

  @Test
  void whenAlignCampaignThenOk() {
    String campaignId = "campaignId";
    OffsetDateTime now = OffsetDateTime.now(Constants.ZONEID);

    Mockito.doNothing().when(campaignServiceMock).alignCampaign(campaignId, now);

    ResponseEntity<Void> response = campaignController.alignCampaign(campaignId, now);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Assertions.assertNull(response.getBody());
  }

  @Test
  void whenRenameCampaignThenOk() {
    String campaignId = "campaignId";
    RenameCampaignRequest request = new RenameCampaignRequest();
    request.setName("NAME");

    Mockito.doNothing().when(campaignServiceMock).renameCampaign(campaignId, request);

    ResponseEntity<Void> response = campaignController.renameCampaign(campaignId, request);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNull(response.getBody());
  }

  @Test
  void whenFindCampaignsByFiltersThenOk() {
    CampaignFiltersDTO campaignFiltersDTO = podamFactory.manufacturePojo(CampaignFiltersDTO.class);
    Pageable pageable = PageRequest.of(0, 10);
    PagedCampaign expectedResponse = podamFactory.manufacturePojo(PagedCampaign.class);

    when(campaignServiceMock.findCampaignsByFilters(campaignFiltersDTO, pageable))
      .thenReturn(expectedResponse);

    ResponseEntity<PagedCampaign> response = campaignController.findCampaignsByFilters(
      campaignFiltersDTO.getOrganizationId(),
      campaignFiltersDTO.getDateFrom(),
      campaignFiltersDTO.getDateTo(),
      campaignFiltersDTO.getOrgSubUnitCodes(),
      campaignFiltersDTO.getCampaignName(),
      campaignFiltersDTO.getExternalCampaignId(),
      campaignFiltersDTO.getFetchAll(),
      pageable);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void whenGetCampaignThenOk() {
    String campaignId = "campaignId";
    Campaign expectedResponse = podamFactory.manufacturePojo(Campaign.class);

    when(campaignServiceMock.getCampaignById(campaignId)).thenReturn(expectedResponse);

    ResponseEntity<Campaign> response = campaignController.getCampaign(campaignId);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void whenFindCampaignSendNotificationsThenOk() {
    SendNotificationFiltersDTO filters = podamFactory.manufacturePojo(SendNotificationFiltersDTO.class);
    filters.setFiscalCodeHash(null);
    String fiscalCode = "fiscalCode";
    Pageable pageable = PageRequest.of(0, 10);
    PagedSendNotifications expectedResponse = podamFactory.manufacturePojo(PagedSendNotifications.class);

    when(campaignServiceMock.getCampaignSendNotifications(filters,fiscalCode,pageable)).thenReturn(expectedResponse);

    ResponseEntity<PagedSendNotifications> response = campaignController.findCampaignSendNotifications(filters.getCampaignId(), filters.getOrganizationId(), filters.getIun(), filters.getDateFrom(), filters.getDateTo(), filters.getStatuses(), fiscalCode, pageable);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void whenFindLatestFullRecalculationDateThenOk() {
    //GIVEN
    OffsetDateTime expectedLatestRecalculationDate = OffsetDateTime.now(Constants.ZONEID);
    when(campaignServiceMock.findLatestFullRecalculationDate())
      .thenReturn(expectedLatestRecalculationDate);
    //WHEN
    ResponseEntity<OffsetDateTime> actualResponseEntity = campaignController.findLatestFullRecalculationDate();
    //THEN
    Assertions.assertEquals(HttpStatus.OK, actualResponseEntity.getStatusCode());
    Assertions.assertEquals(expectedLatestRecalculationDate, actualResponseEntity.getBody());
  }

  @Test
  void whenFindFirstCampaignStartDateThenOk() {
    //GIVEN
    OffsetDateTime expectedFirstCampaignStartDate = OffsetDateTime.now(Constants.ZONEID);
    when(campaignServiceMock.findFirstCampaignStartDate())
      .thenReturn(expectedFirstCampaignStartDate);
    //WHEN
    ResponseEntity<OffsetDateTime> actualResponseEntity = campaignController.findFirstCampaignStartDate();
    //THEN
    Assertions.assertEquals(HttpStatus.OK, actualResponseEntity.getStatusCode());
    Assertions.assertEquals(expectedFirstCampaignStartDate, actualResponseEntity.getBody());
  }

  @Test
  void whenFindIdsOfUpdatedCampaignsByNotificationUpdateDateThenOk() {
    //GIVEN
    OffsetDateTime latestRecalculationDate = OffsetDateTime.now(Constants.ZONEID);
    List<String> expectedIdList = List.of("id1", "id2", "id3");
    when(campaignServiceMock.findIdsOfUpdatedCampaignsByNotificationUpdateDate(latestRecalculationDate))
      .thenReturn(expectedIdList);
    //WHEN
    ResponseEntity<List<String>> actualResponseEntity = campaignController.findIdsOfUpdatedCampaignsByNotificationUpdateDate(latestRecalculationDate);
    //THEN
    Assertions.assertEquals(HttpStatus.OK, actualResponseEntity.getStatusCode());
    Assertions.assertEquals(expectedIdList, actualResponseEntity.getBody());
  }
}
