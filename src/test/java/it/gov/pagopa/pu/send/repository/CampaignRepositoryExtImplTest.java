package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.dto.CampaignFiltersDTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.util.Constants;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignRepositoryExtImplTest extends BaseMongoRepositoryTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private UpdateResult updateResult;

  @InjectMocks
  private CampaignRepositoryExtImpl repository;

  @Test
  void givenIncrementTotalAndUpdateEndDateThenVerify() {
    String campaignId = "campaignId";
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);

    when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.incrementTotalAndUpdateEndDate(campaignId, endDate);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateCampaignCountersThenVerify() {
    String campaignId = "campaignId";
    NotificationStatusChangeDTO notificationStatusChangeDTO = podamFactory.manufacturePojo(NotificationStatusChangeDTO.class);

    when(mongoTemplateMock.updateFirst(
      Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
          Campaign.class)))
      .thenReturn(updateResult);
    when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateCampaignCounters(campaignId, notificationStatusChangeDTO);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateStartDateThenVerify() {
    String campaignId = "campaignId";
    LocalDate startDate = LocalDate.of(2026, Month.JUNE, 30);

    when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateStartDate(campaignId, startDate);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateEndDateThenVerify() {
    String campaignId = "campaignId";
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);

    when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateEndDate(campaignId, endDate);

    assertEquals(1L, result.getModifiedCount());
  }
  @Test
  void givenUpdateCampaignNameThenVerify() {
    String campaignId = "campaignId";
    String campaignName = "campaignName";

    when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
      Mockito.any(Update.class),
      Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateCampaignName(campaignId, campaignName);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void whenFindCampaignsByFiltersThenOk() {
    CampaignFiltersDTO campaignFiltersDTO = podamFactory.manufacturePojo(CampaignFiltersDTO.class);

    List<Campaign> campaigns = podamFactory.manufacturePojo(List.class, Campaign.class);
    Pageable pageable = PageRequest.of(0, campaigns.size());
    int totalElements = campaigns.size() + 1;

    when(mongoTemplateMock.count(Mockito.any(Query.class), Mockito.eq(Campaign.class))).thenReturn(Long.valueOf(totalElements));
    when(mongoTemplateMock.find(Mockito.any(Query.class), Mockito.eq(Campaign.class))).thenReturn(campaigns);

    Page<Campaign> result = repository.findCampaignsByFilters(campaignFiltersDTO, pageable);

    assertEquals(totalElements, result.getTotalElements());
    assertEquals(campaigns, result.getContent());
    assertEquals(pageable.getOffset(), result.getNumber());
    assertEquals(pageable.getPageSize(), result.getSize());
  }

  @Test
  void givenDocumentWithFullRecalculationDateKeyWhenFindLatestFullRecalculationDateThenOk() {
    //GIVEN
    Date expectedFullRecalculationDate = Date.from(OffsetDateTime.now(Constants.ZONEID).toInstant());

    AggregationResults<Document> aggregationResults = new AggregationResults<>(
      List.of(new Document(Counters.Fields.fullRecalculationDate, expectedFullRecalculationDate)),
      new Document()
    );

    when(mongoTemplateMock.aggregate(
      Mockito.any(Aggregation.class),
      Mockito.eq(Campaign.class),
      Mockito.eq(Document.class)
    )).thenReturn(aggregationResults);

    //WHEN
    OffsetDateTime actualFullRecalculationOffsetDateTime = repository.findLatestFullRecalculationDate();

    //THEN
    Assertions.assertNotNull(actualFullRecalculationOffsetDateTime);
    Assertions.assertEquals(
      OffsetDateTime.ofInstant(expectedFullRecalculationDate.toInstant(), Constants.ZONEID),
      actualFullRecalculationOffsetDateTime
    );
  }

  @Test
  void givenDocumentWithoutFullRecalculationDateKeyWhenFindLatestFullRecalculationDateThenNull() {
    //GIVEN
    AggregationResults<Document> aggregationResults = new AggregationResults<>(
      List.of(new Document()),
      new Document()
    );

    when(mongoTemplateMock.aggregate(
      Mockito.any(Aggregation.class),
      Mockito.eq(Campaign.class),
      Mockito.eq(Document.class)
    )).thenReturn(aggregationResults);

    //WHEN
    OffsetDateTime actualLatestFullRecalculationDate = repository.findLatestFullRecalculationDate();

    //THEN
    Assertions.assertNull(actualLatestFullRecalculationDate);
  }

  @Test
  void givenNoDocumentWhenFindLatestFullRecalculationDateThenNull() {
    //GIVEN
    AggregationResults<Document> aggregationResults = new AggregationResults<>(
      List.of(),
      new Document()
    );

    when(mongoTemplateMock.aggregate(
      Mockito.any(Aggregation.class),
      Mockito.eq(Campaign.class),
      Mockito.eq(Document.class)
    )).thenReturn(aggregationResults);

    //WHEN
    OffsetDateTime actualLatestFullRecalculationDate = repository.findLatestFullRecalculationDate();

    //THEN
    Assertions.assertNull(actualLatestFullRecalculationDate);
  }
}
