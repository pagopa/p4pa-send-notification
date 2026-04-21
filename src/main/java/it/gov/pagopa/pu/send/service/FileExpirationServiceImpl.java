package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.service.BrokerConfigurationService;
import it.gov.pagopa.pu.send.dto.generated.FileExpirationResponseDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.exception.ExpirationConfigNotFoundException;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Service
public class FileExpirationServiceImpl implements FileExpirationService {
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final BrokerConfigurationService brokerConfigurationService;
  private final FileStorerService fileStorerService;
  private final SendNotificationService sendNotificationService;

  public FileExpirationServiceImpl(SendNotificationNoPIIRepository sendNotificationNoPIIRepository, BrokerConfigurationService brokerConfigurationService, FileStorerService fileStorerService, SendNotificationService sendNotificationService) {
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.brokerConfigurationService = brokerConfigurationService;
    this.fileStorerService = fileStorerService;
    this.sendNotificationService = sendNotificationService;
  }

  @Override
  public FileExpirationResponseDTO deleteExpiredLegalFacts(String sendNotificationId, String accessToken) {
    SendNotificationNoPII sendNotification = sendNotificationService.findSendNotification(sendNotificationId);

    BrokerConfiguration brokerConfiguration = brokerConfigurationService.getBrokerConfigurationByOrganizationId(sendNotification.getOrganizationId(), accessToken);
    if(brokerConfiguration == null || brokerConfiguration.getLegalFactsExpirationDays()==null) {
      throw new ExpirationConfigNotFoundException("legalFactsExpirationDays is not configured for the organization having organizationId "+sendNotification.getOrganizationId());
    }
    Long legalFactsExpirationDays = brokerConfiguration.getLegalFactsExpirationDays();

    OffsetDateTime nextFileExpirationDate = sendNotification.getLegalFacts().stream()
      .filter(legalFact -> legalFact.getStatus() != FileStatus.EXPIRED && legalFact.getDownloadDate() != null)
      .map(legalFact -> processFile(
        sendNotification,
        legalFact.getFileName(),
        legalFact.getDownloadDate().plusDays(legalFactsExpirationDays),
        OffsetDateTime.now(),
        ()-> sendNotificationNoPIIRepository.updateLegalFactStatus(sendNotificationId, legalFact.getFileName(), FileStatus.EXPIRED)))
      .filter(Objects::nonNull)
      .max(Comparator.naturalOrder())
      .orElse(null);

    return FileExpirationResponseDTO.builder()
      .nextFileExpirationDate(nextFileExpirationDate)
      .build();
  }

  private OffsetDateTime processFile(SendNotificationNoPII notification, String fileName, OffsetDateTime fileExpirationDateTime, OffsetDateTime scheduledDateTime, Supplier<UpdateResult> updateAction) {
    if (scheduledDateTime.isAfter(fileExpirationDateTime)) {
      deleteFileAndUpdate(notification.getSendNotificationId(), fileName, notification.getOrganizationId(), updateAction);
      return null;
    }
    return fileExpirationDateTime;
  }

  private void deleteFileAndUpdate(String sendNotificationId, String fileName, Long organizationId, Supplier<UpdateResult> updateAction) {
    fileStorerService.deleteFromSharedFolder(organizationId, sendNotificationId, fileName);
    UpdateResult updateResult = updateAction.get();
    if (updateResult.getModifiedCount() != 1L) {
      log.debug("Cannot update send notification file having sendNotificationId {} filename {} to status {}",sendNotificationId, fileName, FileStatus.EXPIRED);
    }
  }
}
