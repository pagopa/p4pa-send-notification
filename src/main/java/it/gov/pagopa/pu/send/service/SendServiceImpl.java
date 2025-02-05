package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.client.SendClientImpl;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import java.util.ArrayList;
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
    List<PreLoadRequestDTO> preLoadRequest = new ArrayList<>();

    sendNotificationRepository.findById(sendNotificationId).ifPresent(notification -> {
      NotificationUtils.validateStatus(NotificationStatus.SENDING, notification.getStatus());
      preLoadRequest.addAll(notification.getDocuments().stream().map(document -> {
        NotificationUtils.validateStatus(FileStatus.READY,document.getStatus());
        PreLoadRequestDTO preLoadFile = new PreLoadRequestDTO();
        preLoadFile.setPreloadIdx(document.getFileName());
        preLoadFile.setContentType(document.getContentType());
        preLoadFile.setSha256(document.getDigest());
        return preLoadFile;
      }).toList());
    });
    sendClient.preloadFiles(preLoadRequest);
  }
}
