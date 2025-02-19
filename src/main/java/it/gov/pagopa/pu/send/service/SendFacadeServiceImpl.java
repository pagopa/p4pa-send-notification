package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendFacadeServiceImpl implements SendFacadeService {
  private final SendNotificationRepository sendNotificationRepository;
  private final SendClient sendClient;
  private final SendUploadFacadeServiceImpl uploadService;
  private final SendNotification2NewNotificationRequestMapper sendNotificationMapper;

  public SendFacadeServiceImpl(SendNotificationRepository sendNotificationRepository,
                               SendClient sendClient, SendUploadFacadeServiceImpl uploadService,
                               SendNotification2NewNotificationRequestMapper sendNotificationMapper) {
    this.sendNotificationRepository = sendNotificationRepository;
    this.sendClient = sendClient;
    this.uploadService = uploadService;
    this.sendNotificationMapper = sendNotificationMapper;
  }

  @Override
  public void preloadFiles(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.SENDING, notification.getStatus());
    List<PreLoadRequestDTO> preLoadRequest = notification.getDocuments().stream()
      .map(doc -> {
        NotificationUtils.validateStatus(FileStatus.READY, doc.getStatus());
        PreLoadRequestDTO preLoadFile = new PreLoadRequestDTO();
        preLoadFile.setPreloadIdx(doc.getFileName());
        preLoadFile.setContentType(doc.getContentType());
        preLoadFile.setSha256(doc.getDigest());
        return preLoadFile;
      }).toList();

    //Call SEND preload API
    List<PreLoadResponseDTO> preLoadResponseDTO = sendClient.preloadFiles(preLoadRequest);
    preLoadResponseDTO.forEach(response ->
      sendNotificationRepository.updateFilePreloadInformation(sendNotificationId, response));

    sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.REGISTERED);
  }

  @Override
  public void uploadFiles(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.REGISTERED, notification.getStatus());
    for(DocumentDTO doc : notification.getDocuments()){
      Optional<String> versionId = Optional.empty();
      if(!doc.getStatus().equals(FileStatus.UPLOADED))
        versionId = uploadService.uploadFile(sendNotificationId, doc);
      if (versionId.isPresent()) {
        sendNotificationRepository.updateFileStatus(sendNotificationId, doc.getFileName(), FileStatus.UPLOADED);
        sendNotificationRepository.updateFileVersionId(sendNotificationId, doc.getFileName(), versionId.get());
      }
    }
    sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.UPLOADED);
  }

  @Override
  public void deliveryNotification(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.UPLOADED, notification.getStatus());
    NewNotificationResponseDTO responseDTO = sendClient.deliveryNotification(sendNotificationMapper.apply(notification));
    if (responseDTO!=null){
      sendNotificationRepository.updateNotificationRequestId(sendNotificationId, responseDTO.getNotificationRequestId());
      sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
    }
  }

  @Override
  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.COMPLETE, notification.getStatus());
    NewNotificationRequestStatusResponseV24DTO notificationStatus = sendClient.notificationStatus(notification.getNotificationRequestId());
    if(notificationStatus!=null && notificationStatus.getIun() != null)
      sendNotificationRepository.updateNotificationIun(sendNotificationId, notificationStatus.getIun());

    return notificationStatus;
  }

  private SendNotification findSendNotification(String sendNotificationId) {
    return sendNotificationRepository.findById(sendNotificationId)
      .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + sendNotificationId));
  }
}
