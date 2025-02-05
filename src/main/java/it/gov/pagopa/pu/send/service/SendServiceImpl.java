package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.client.SendClientImpl;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendServiceImpl implements SendService{

  private final SendNotificationRepository sendNotificationRepository;
  private final SendClientImpl sendClient;

  public SendServiceImpl(SendNotificationRepository sendNotificationRepository,
    SendClientImpl sendClient) {
    this.sendNotificationRepository = sendNotificationRepository;
    this.sendClient = sendClient;
  }

  @Override
  public void preloadFiles(String sendNotificationId) {
    SendNotification notification = sendNotificationRepository.findById(sendNotificationId)
      .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + sendNotificationId));

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
}
