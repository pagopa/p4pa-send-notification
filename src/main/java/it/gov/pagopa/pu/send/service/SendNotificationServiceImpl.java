package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.dto.generated.StartNotificationResponse;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.FileUtils;
import it.gov.pagopa.pu.send.util.NotificationUtils;

import java.io.File;

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
  public CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest, Long organizationId) {
    SendNotification sendNotification = sendNotificationRepository.insert(mapper.map(createNotificationRequest, organizationId));

    return CreateNotificationResponse
      .builder()
      .sendNotificationId(sendNotification.getSendNotificationId())
      .status(sendNotification.getStatus().name())
      //.preloadRef() TODO P4ADEV-2080 comunicate fileName and url for upload file
      .build();
  }

  @Override
  public StartNotificationResponse startSendNotification(String sendNotificationId, Long organizationId, LoadFileRequest loadFileRequest) {
    SendNotification notification = findSendNotification(sendNotificationId, organizationId);
    NotificationUtils.validateStatus(NotificationStatus.WAITING_FILE, notification.getStatus());

    notification.getDocuments().stream()
      .filter(doc -> doc.getFileName().equals(loadFileRequest.getFileName()))
      .findFirst().ifPresentOrElse(
        doc -> updateFileStatus(sendNotificationId, doc, loadFileRequest),
        () -> {
          throw new IllegalArgumentException("File not found with id: " + loadFileRequest.getFileName());
        }
      );

    notification = findSendNotification(sendNotificationId, organizationId);
    boolean allFilesReady = notification.getDocuments().stream()
      .allMatch(doc -> doc.getStatus().equals(FileStatus.READY));

    if (allFilesReady) {
      sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.SENDING);
      // TODO P4ADEV-2232 invoke temporal to start workflow
      return StartNotificationResponse.builder().workFlowId(sendNotificationId).build();
    }
    return null;
  }

  @Override
  public void deleteSendNotification(String sendNotificationId, Long organizationId) {
    SendNotification notification = findSendNotification(sendNotificationId, organizationId);
    if (!notification.getStatus().equals(NotificationStatus.COMPLETE))
      sendNotificationRepository.deleteById(sendNotificationId);
    else
      throw new InvalidStatusException("Cannot delete notification with status complete");
  }


  private SendNotification findSendNotification(String sendNotificationId, Long organizationId) {
    return sendNotificationRepository.findByIdAndOrganizationId(sendNotificationId, organizationId)
      .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + sendNotificationId + " for organizationId: " + organizationId));
  }

  private void updateFileStatus(String sendNotificationId, DocumentDTO doc, LoadFileRequest loadFileRequest) {
    NotificationUtils.validateStatus(FileStatus.WAITING, doc.getStatus());
    //TODO edit file retrieve with P4ADEV-2148, change static sendNotificationId with doc.getSendNotificationId
    String filePath;
    if (loadFileRequest.getPath() != null)
      filePath = loadFileRequest.getPath() + "sendNotificationId_" + doc.getFileName();
    else
      filePath = "src/main/resources/tmp/" + "sendNotificationId_" + doc.getFileName();

    try {
      File file = new File(filePath);
      if (!FileUtils.calculateFileHash(file).equals(loadFileRequest.getDigest()))
        throw new InvalidSignatureException("File " + doc.getFileName() + " has not a valid signature");
    } catch (Exception e) {
      throw new InvalidSignatureException(e.getMessage());
    }
    sendNotificationRepository.updateFileStatus(sendNotificationId, doc.getFileName(), FileStatus.READY);
  }
}
