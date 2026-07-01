package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.incrementTotalAndUpdateEndDate(campaignId, endDate);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateCampaignCountersThenVerify() {
    String campaignId = "campaignId";
    NotificationStatusChangeDTO notificationStatusChangeDTO = podamFactory.manufacturePojo(NotificationStatusChangeDTO.class);

    Mockito.when(mongoTemplateMock.updateFirst(
      Mockito.any(Query.class), Mockito.any(Update.class), Mockito.eq(
          Campaign.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateCampaignCounters(campaignId, notificationStatusChangeDTO);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateStartDateThenVerify() {
    String campaignId = "campaignId";
    LocalDate startDate = LocalDate.of(2026, Month.JUNE, 30);

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateStartDate(campaignId, startDate);

    assertEquals(1L, result.getModifiedCount());
  }

  @Test
  void givenUpdateEndDateThenVerify() {
    String campaignId = "campaignId";
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);

    Mockito.when(mongoTemplateMock.updateFirst(Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(Campaign.class)))
      .thenReturn(updateResult);
    Mockito.when(updateResult.getModifiedCount()).thenReturn(1L);

    UpdateResult result = repository.updateEndDate(campaignId, endDate);

    assertEquals(1L, result.getModifiedCount());
  }

}
