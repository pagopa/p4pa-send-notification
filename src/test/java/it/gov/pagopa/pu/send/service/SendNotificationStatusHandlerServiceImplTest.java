package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.NotificationStatusChangeDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.Campaign;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.Constants;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationStatusHandlerServiceImplTest {
  public static final uk.co.jemos.podam.api.PodamFactory podamFactory = TestUtils.getPodamFactory();
  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private CampaignService campaignServiceMock;
  @InjectMocks
  private SendNotificationStatusHandlerServiceImpl sendNotificationStatusHandlerService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendNotificationNoPIIRepositoryMock,
      campaignServiceMock
    );
  }

  @Test
  void whenHandleNewSendNotificationThenOk() {
    CreateNotificationRequest createNotificationRequest = podamFactory.manufacturePojo(CreateNotificationRequest.class);
    LocalDate creationDate = LocalDate.of(2026, Month.JUNE, 30);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    try(MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(LocalDate.class)) {
      localDateMockedStatic.when(() -> LocalDate.now(Constants.ZONEID)).thenReturn(creationDate);
      when(campaignServiceMock.createIfNotExists(
        createNotificationRequest.getExternalCampaignId(),
        createNotificationRequest.getCampaignName(),
        createNotificationRequest,
        creationDate
      )).thenReturn(campaign);
      Mockito.doNothing().when(campaignServiceMock).incrementTotalAndUpdateEndDate(campaign.getCampaignId(),creationDate);

      Campaign result = sendNotificationStatusHandlerService.handleNewSendNotification(createNotificationRequest);

      Assertions.assertNotNull(result);
      Assertions.assertEquals(campaign,result);
    }
  }

  @Test
  void whenHandleSendNotificationStatusUpdateThenIncrFieldsPopulated() {
    String sendNotificationId = "sendNotificationId";
    String campaignId = "campaignId";
    TimelineElementCategoryV27DTO oldStatus = TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS;
    TimelineElementCategoryV27DTO newStatus = TimelineElementCategoryV27DTO.SEND_DIGITAL_FEEDBACK;
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .incrFields(Set.of(Counters.Fields.digitalCompleted))
      .decrFields(Set.of(Counters.Fields.completion))
      .build();

    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);
    when(sendNotificationNoPIIRepositoryMock.updateStreamEventStatusById(sendNotificationId, newStatus))
      .thenReturn(updateResult);

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      sendNotificationId, campaignId, oldStatus, newStatus));
  }

  @Test
  void givenStatusDowngradeWhenHandleSendNotificationStatusUpdateThenDecrFieldsPopulated() {
    String sendNotificationId = "sendNotificationId";
    String campaignId = "campaignId";
    TimelineElementCategoryV27DTO oldStatus = TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK;
    TimelineElementCategoryV27DTO newStatus = TimelineElementCategoryV27DTO.REQUEST_ACCEPTED;
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .incrFields(Set.of())
      .decrFields(Set.of(Counters.Fields.delivered, Counters.Fields.analogicCompleted))
      .build();

    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);
    when(sendNotificationNoPIIRepositoryMock.updateStreamEventStatusById(sendNotificationId, newStatus))
      .thenReturn(updateResult);

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      sendNotificationId, campaignId, oldStatus, newStatus));
  }

  @Test
  void givenSameStatusWhenHandleSendNotificationStatusUpdateThenNoUpdate() {
    String sendNotificationId = "sendNotificationId";
    String campaignId = "campaignId";
    TimelineElementCategoryV27DTO status = TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS;

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      sendNotificationId, campaignId, status, status));
  }

  @Test
  void givenNullOldStatusWhenHandleSendNotificationStatusUpdateThenIncrFieldsPopulated() {
    String sendNotificationId = "sendNotificationId";
    String campaignId = "campaignId";
    TimelineElementCategoryV27DTO newStatus = TimelineElementCategoryV27DTO.REQUEST_ACCEPTED;
    UpdateResult updateResult = podamFactory.manufacturePojo(UpdateResult.class);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .incrFields(Set.of(Counters.Fields.accepted))
      .decrFields(Set.of())
      .build();

    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);
    when(sendNotificationNoPIIRepositoryMock.updateStreamEventStatusById(sendNotificationId, newStatus))
      .thenReturn(updateResult);

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleSendNotificationStatusUpdate(
      sendNotificationId, campaignId, null, newStatus));
  }

  @Test
  void givenLastNotificationWhenHandleDeletedSendNotificationThenDeleteCampaign() {
    String campaignId = "campaignId";
    LocalDate notificationCreationDate = LocalDate.of(2026, Month.JUNE, 30);
    TimelineElementCategoryV27DTO streamEventStatus = TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS;

    Counters counters = new Counters();
    counters.setTotal(1L);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    campaign.setCounters(counters);

    when(campaignServiceMock.getCampaignById(campaignId)).thenReturn(campaign);
    Mockito.doNothing().when(campaignServiceMock).deleteCampaignById(campaignId);

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleDeletedSendNotification(
      campaignId, notificationCreationDate, streamEventStatus));
  }
  @Test
  void givenStartDateEqualsNotificationCreationDateWhenHandleDeletedSendNotificationThenUpdateStartDate() {
    String campaignId = "campaignId";
    LocalDate notificationCreationDate = LocalDate.of(2026, Month.JUNE, 30);
    LocalDate endDate = LocalDate.of(2026, Month.JULY, 5);
    TimelineElementCategoryV27DTO currentStreamEventStatus = TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS;

    Counters counters = new Counters();
    counters.setTotal(2L);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    campaign.setCounters(counters);
    campaign.setStartDate(notificationCreationDate);
    campaign.setEndDate(endDate);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .decrFields(Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion, Counters.Fields.total))
      .build();

    SendNotificationNoPII sendNotification = podamFactory.manufacturePojo(SendNotificationNoPII.class);
    LocalDateTime newStartCreationDate = LocalDateTime.of(2026, Month.JULY, 3, 0, 0);
    sendNotification.setCreationDate(newStartCreationDate);

    when(campaignServiceMock.getCampaignById(campaignId)).thenReturn(campaign);
    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);
    when(sendNotificationNoPIIRepositoryMock.findTopByCampaignIdOrderByCreationDateAsc(campaignId))
      .thenReturn(sendNotification);
    Mockito.doNothing().when(campaignServiceMock).updateStartDate(campaignId, newStartCreationDate.toLocalDate());

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleDeletedSendNotification(
      campaignId, notificationCreationDate, currentStreamEventStatus));
  }

  @Test
  void givenEndDateEqualsNotificationCreationDateWhenHandleDeletedSendNotificationThenUpdateEndDate() {
    String campaignId = "campaignId";
    LocalDate notificationCreationDate = LocalDate.of(2026, Month.JUNE, 30);
    LocalDate startDate = LocalDate.of(2026, Month.JUNE, 1);
    TimelineElementCategoryV27DTO currentStreamEventStatus = TimelineElementCategoryV27DTO.SEND_DIGITAL_FEEDBACK;

    Counters counters = new Counters();
    counters.setTotal(2L);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    campaign.setCounters(counters);
    campaign.setStartDate(startDate);
    campaign.setEndDate(notificationCreationDate);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .decrFields(Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.digitalCompleted, Counters.Fields.total))
      .build();

    SendNotificationNoPII sendNotification = podamFactory.manufacturePojo(SendNotificationNoPII.class);
    LocalDateTime newEndCreationDate = LocalDateTime.of(2026, Month.JUNE, 29, 0, 0);
    sendNotification.setCreationDate(newEndCreationDate);

    when(campaignServiceMock.getCampaignById(campaignId)).thenReturn(campaign);
    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);
    when(sendNotificationNoPIIRepositoryMock.findTopByCampaignIdOrderByCreationDateDesc(campaignId))
      .thenReturn(sendNotification);
    Mockito.doNothing().when(campaignServiceMock).updateEndDate(campaignId, newEndCreationDate.toLocalDate());

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleDeletedSendNotification(
      campaignId, notificationCreationDate, currentStreamEventStatus));
  }

  @Test
  void givenNeitherStartNorEndDateEqualsWhenHandleDeletedSendNotificationThenOnlyHandleStatusChange() {
    String campaignId = "campaignId";
    LocalDate notificationCreationDate = LocalDate.of(2026, Month.JUNE, 15);
    LocalDate startDate = LocalDate.of(2026, Month.JUNE, 1);
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);
    TimelineElementCategoryV27DTO currentStreamEventStatus = TimelineElementCategoryV27DTO.REQUEST_ACCEPTED;

    Counters counters = new Counters();
    counters.setTotal(2L);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    campaign.setCounters(counters);
    campaign.setStartDate(startDate);
    campaign.setEndDate(endDate);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .decrFields(Set.of(Counters.Fields.accepted, Counters.Fields.total))
      .build();

    when(campaignServiceMock.getCampaignById(campaignId)).thenReturn(campaign);
    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleDeletedSendNotification(
      campaignId, notificationCreationDate, currentStreamEventStatus));
  }

  @Test
  void givenNoStreamEventStatusWhenHandleDeletedSendNotificationThenOnlyTotalDecremented() {
    String campaignId = "campaignId";
    LocalDate notificationCreationDate = LocalDate.of(2026, Month.JUNE, 15);
    LocalDate startDate = LocalDate.of(2026, Month.JUNE, 1);
    LocalDate endDate = LocalDate.of(2026, Month.JUNE, 30);
    TimelineElementCategoryV27DTO currentStreamEventStatus = null;

    Counters counters = new Counters();
    counters.setTotal(2L);
    Campaign campaign = podamFactory.manufacturePojo(Campaign.class);
    campaign.setCounters(counters);
    campaign.setStartDate(startDate);
    campaign.setEndDate(endDate);

    NotificationStatusChangeDTO notificationStatusChangeDTO = NotificationStatusChangeDTO.builder()
      .decrFields(Set.of(Counters.Fields.total))
      .build();

    when(campaignServiceMock.getCampaignById(campaignId)).thenReturn(campaign);
    Mockito.doNothing().when(campaignServiceMock).handleStatusChange(campaignId, notificationStatusChangeDTO);

    Assertions.assertDoesNotThrow(() -> sendNotificationStatusHandlerService.handleDeletedSendNotification(
      campaignId, notificationCreationDate, currentStreamEventStatus));
  }
}
