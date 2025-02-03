package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.mapper.SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationRepository sendNotificationRepository;
  private final SendNotificationMapper sendNotificationMapper;
  private final SequenceGeneratorService sequenceGeneratorService;

  public SendNotificationServiceImpl(SendNotificationRepository sendNotificationRepository,
    SendNotificationMapper sendNotificationMapper,
    SequenceGeneratorService sequenceGeneratorService) {
    this.sendNotificationRepository = sendNotificationRepository;
    this.sendNotificationMapper = sendNotificationMapper;
    this.sequenceGeneratorService = sequenceGeneratorService;
  }

  @Override
  public CreateNotificationResponse createSendNotification(
    List<CreateNotificationRequest> newNotifications) {

    Long sendNotificationId = sequenceGeneratorService.generateSequence(SendNotification.SEQUENCE_NAME);
    for(CreateNotificationRequest newNotification : newNotifications) {
      sendNotificationRepository.createIfNotExists(sendNotificationId, sendNotificationMapper.apply(newNotification));
    }

    return CreateNotificationResponse
      .builder()
      .sendNotificationId(sendNotificationId)
      .status(NotificationStatus.WAITING_FILE.name())
      //.preloadRef() TODO P4ADEV-2080 comunicate fileid and url for upload file
      .build();
  }
}
