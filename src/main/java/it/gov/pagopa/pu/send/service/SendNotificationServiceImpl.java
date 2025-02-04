package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationRepository sendNotificationRepository;
  private final SequenceGeneratorService sequenceGeneratorService;

  public SendNotificationServiceImpl(SendNotificationRepository sendNotificationRepository,
    SequenceGeneratorService sequenceGeneratorService) {
    this.sendNotificationRepository = sendNotificationRepository;
    this.sequenceGeneratorService = sequenceGeneratorService;
  }

  @Override
  public CreateNotificationResponse createSendNotification(
    List<CreateNotificationRequest> newNotifications) {

    Long sendNotificationId = sequenceGeneratorService.generateSequence("send_notification_sequence");
    for(CreateNotificationRequest newNotification : newNotifications) {
      sendNotificationRepository.createIfNotExists(sendNotificationId, newNotification);
    }

    return CreateNotificationResponse
      .builder()
      .sendNotificationId(sendNotificationId)
      .status(NotificationStatus.WAITING_FILE.name())
      //.preloadRef() TODO P4ADEV-2080 comunicate fileid and url for upload file
      .build();
  }
}
