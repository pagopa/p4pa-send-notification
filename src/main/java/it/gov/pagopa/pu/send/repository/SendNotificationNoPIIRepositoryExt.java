package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.SendNotificationFiltersDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SendNotificationNoPIIRepositoryExt {
  UpdateResult updateFilePreloadInformation(String sendNotificationId, PreLoadResponseDTO preLoad);
  UpdateResult updateNotificationStatus(String notificationRequestId, NotificationStatus newStatus);
  UpdateResult updateNotificationStatusById(String sendNotificationId, NotificationStatus newStatus);
  UpdateResult updateNotificationRequestId(String sendNotificationId, String notificationRequestId);
  UpdateResult updateFileStatus(String sendNotificationId, String fileName, FileStatus newStatus);
  UpdateResult updateFileStatusAndUploadDate(String sendNotificationId, String fileName, FileStatus newStatus, OffsetDateTime uploadDate);
  UpdateResult updateFileVersionId(String sendNotificationId, String fileName, String versionId);
  UpdateResult updateNotificationIun(String sendNotificationId, String iun);
  UpdateResult updateNotificationDate(String sendNotificationId, OffsetDateTime notificationDate, String nav);
  Optional<SendNotificationNoPII> findByIdAndOrganizationId(String notificationId, Long organizationId);
  Optional<SendNotificationNoPII> findByOrganizationIdAndNav(Long organizationId, String nav);
  Optional<SendNotificationNoPII> findByNotificationRequestId(String notificationRequestId);
  UpdateResult addLegalFact(String sendNotificationId, LegalFactDTO legalFact);
  UpdateResult updateLegalFactStatus(String sendNotificationId, String fileName, FileStatus status);
  Counters calculateCampaignCounters(String campaignId);
  UpdateResult updateLastEventOfInterestById(String sendNotificationId, TimelineElementCategoryV27DTO newStatus);
  List<StreamEventSummaryDTO> pushStreamEventsHistory(String sendNotificationId, List<StreamEventSummaryDTO> streamEvents);
  Page<SendNotificationNoPII> findSendNotificationsByFilters(SendNotificationFiltersDTO sendNotificationFiltersDTO, Pageable pageable);
}
