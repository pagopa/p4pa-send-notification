package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.send.connector.organization.service.BrokerConfigurationService;
import it.gov.pagopa.pu.send.dto.generated.FileExpirationResponseDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.exception.ExpirationConfigNotFoundException;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileExpirationServiceImplTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private BrokerConfigurationService brokerConfigurationServiceMock;
  @Mock
  private SendNotificationService sendNotificationServiceMock;

  @InjectMocks
  private FileExpirationServiceImpl fileExpirationService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendNotificationNoPIIRepositoryMock, fileStorerServiceMock, brokerConfigurationServiceMock, sendNotificationServiceMock);
  }

  @Test
  void givenNullBrokerConfigurationWhenDeleteExpiredLegalFactsThenExpirationConfigNotFoundException() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long organizationId = 1L;

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);

    Mockito.when(sendNotificationServiceMock.findSendNotification(sendNotificationId))
      .thenReturn(notification);
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(null);

    ExpirationConfigNotFoundException expirationConfigNotFoundException = Assertions.assertThrows(ExpirationConfigNotFoundException.class, () -> fileExpirationService.deleteExpiredLegalFacts(sendNotificationId, accessToken));

    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_EXPIRATION_CONFIG_NOT_FOUND,expirationConfigNotFoundException.getCode());
  }

  @Test
  void givenBrokerConfigurationWithNullExpirationDaysWhenDeleteExpiredLegalFactsThenExpirationConfigNotFoundException() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long organizationId = 1L;

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(null);

    Mockito.when(sendNotificationServiceMock.findSendNotification(sendNotificationId))
      .thenReturn(notification);
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    ExpirationConfigNotFoundException expirationConfigNotFoundException = Assertions.assertThrows(ExpirationConfigNotFoundException.class, () -> fileExpirationService.deleteExpiredLegalFacts(sendNotificationId, accessToken));

    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_EXPIRATION_CONFIG_NOT_FOUND,expirationConfigNotFoundException.getCode());
  }

  @Test
  void givenLegalFactAlreadyExpiredWhenDeleteExpiredLegalFactsThenSkipItAndReturnNullNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;

    LegalFactDTO expiredLegalFact = LegalFactDTO.builder()
      .fileName("already-expired.pdf")
      .status(FileStatus.EXPIRED)
      .downloadDate(OffsetDateTime.now().minusDays(60))
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(expiredLegalFact));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    Mockito.when(sendNotificationServiceMock.findSendNotification(sendNotificationId))
      .thenReturn(notification);
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      fileExpirationService.deleteExpiredLegalFacts(sendNotificationId, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getNextFileExpirationDate());
  }

  @Test
  void givenLegalFactWithNullDownloadDateWhenDeleteExpiredLegalFactsThenSkipItAndReturnNullNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;

    LegalFactDTO legalFactNoDownload = LegalFactDTO.builder()
      .fileName("no-download.pdf")
      .status(FileStatus.READY)
      .downloadDate(null)
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(legalFactNoDownload));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    Mockito.when(sendNotificationServiceMock.findSendNotification(sendNotificationId))
      .thenReturn(notification);
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      fileExpirationService.deleteExpiredLegalFacts(sendNotificationId, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getNextFileExpirationDate());
  }

  @Test
  void givenNoExpiredLegalFactsWhenDeleteExpiredLegalFactsThenReturnNextSchedule() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;

    OffsetDateTime downloadDate1 = OffsetDateTime.now().minusDays(5);
    OffsetDateTime downloadDate2 = OffsetDateTime.now().minusDays(2);
    OffsetDateTime downloadDate3 = OffsetDateTime.now().minusDays(10);

    OffsetDateTime expectedNextFileExpirationDate = downloadDate2.plusDays(expirationDays);

    LegalFactDTO legalFact1 = LegalFactDTO.builder()
      .fileName("file1.pdf")
      .status(FileStatus.READY)
      .downloadDate(downloadDate1)
      .build();

    LegalFactDTO legalFact2 = LegalFactDTO.builder()
      .fileName("file2.pdf")
      .status(FileStatus.READY)
      .downloadDate(downloadDate2)
      .build();

    LegalFactDTO legalFact3 = LegalFactDTO.builder()
      .fileName("file3.pdf")
      .status(FileStatus.READY)
      .downloadDate(downloadDate3)
      .build();

    SendNotificationNoPII notification = new SendNotificationNoPII();
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(legalFact1, legalFact2, legalFact3));

    BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);

    Mockito.when(sendNotificationServiceMock.findSendNotification(sendNotificationId))
      .thenReturn(notification);
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);

    FileExpirationResponseDTO result =
      fileExpirationService.deleteExpiredLegalFacts(sendNotificationId, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedNextFileExpirationDate, result.getNextFileExpirationDate());
  }

  @Test
  void givenPartiallyExpiredLegalFactsWhenDeleteExpiredLegalFactsThenReturnMaxOfRemaining() {
    String accessToken = "accessToken";
    String sendNotificationId = "sendNotificationId";
    long expirationDays = 30L;
    long organizationId = 1L;

    OffsetDateTime recentDownloadDate = OffsetDateTime.now().minusDays(5);
    OffsetDateTime olderDownloadDate  = OffsetDateTime.now().minusDays(10);
    OffsetDateTime expiredDownloadDate = OffsetDateTime.now().minusDays(60);

    OffsetDateTime expectedNextFileExpirationDate = recentDownloadDate.plusDays(expirationDays);

    LegalFactDTO notExpiredFact1 = LegalFactDTO.builder()
      .fileName("not-expired-1.pdf")
      .status(FileStatus.READY)
      .downloadDate(recentDownloadDate)
      .build();

    LegalFactDTO notExpiredFact2 = LegalFactDTO.builder()
      .fileName("not-expired-2.pdf")
      .status(FileStatus.READY)
      .downloadDate(olderDownloadDate)
      .build();

    String expiredFileName = "expired.pdf";
    LegalFactDTO expiredFact = LegalFactDTO.builder()
      .fileName(expiredFileName)
      .status(FileStatus.READY)
      .downloadDate(expiredDownloadDate)
      .build();

    SendNotificationNoPII notification = podamFactory.manufacturePojo(SendNotificationNoPII.class);
    notification.setSendNotificationId(sendNotificationId);
    notification.setOrganizationId(organizationId);
    notification.setLegalFacts(List.of(notExpiredFact1, notExpiredFact2, expiredFact));

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);
    brokerConfiguration.setLegalFactsExpirationDays(expirationDays);
    UpdateResult updateResult = UpdateResult.acknowledged(1, organizationId, null);

    Mockito.when(sendNotificationServiceMock.findSendNotification(sendNotificationId))
      .thenReturn(notification);
    Mockito.when(brokerConfigurationServiceMock.getBrokerConfigurationByOrganizationId(organizationId, accessToken))
      .thenReturn(brokerConfiguration);
    Mockito.when(sendNotificationNoPIIRepositoryMock.updateLegalFactStatus(sendNotificationId, expiredFileName, FileStatus.EXPIRED))
      .thenReturn(updateResult);
    Mockito.doNothing().when(fileStorerServiceMock).deleteFromSharedFolder(organizationId,sendNotificationId,expiredFileName);

    FileExpirationResponseDTO result =
      fileExpirationService.deleteExpiredLegalFacts(sendNotificationId, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedNextFileExpirationDate, result.getNextFileExpirationDate());
  }
}
