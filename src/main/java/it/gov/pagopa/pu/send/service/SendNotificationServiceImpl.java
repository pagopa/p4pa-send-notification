package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.enums.Status;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationRepository sendNotificationRepository;
  private final CreateNotificationRequest2SendNotificationMapper mapper;

  public SendNotificationServiceImpl(SendNotificationRepository sendNotificationRepository,
    CreateNotificationRequest2SendNotificationMapper mapper) {
    this.sendNotificationRepository = sendNotificationRepository;
    this.mapper = mapper;
  }

  @Override
  public CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest) {
    SendNotification sendNotification = sendNotificationRepository.insert(mapper.map(createNotificationRequest));

    return CreateNotificationResponse
      .builder()
      .sendNotificationId(sendNotification.getSendNotificationId())
      .status(Status.WAITING_FILE.name())
      //.preloadRef() TODO P4ADEV-2080 comunicate fileName and url for upload file
      .build();
  }
}
