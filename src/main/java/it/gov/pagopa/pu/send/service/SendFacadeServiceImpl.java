package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SendFacadeServiceImpl implements SendFacadeService {
  private final SendNotificationRepository sendNotificationRepository;
  private final SendClient sendClient;
  private final SendUploadFacadeServiceImpl uploadService;
  private final SendNotification2NewNotificationRequestMapper sendNotificationMapper;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;

  public SendFacadeServiceImpl(SendNotificationRepository sendNotificationRepository,
                               SendClient sendClient, SendUploadFacadeServiceImpl uploadService,
                               SendNotification2NewNotificationRequestMapper sendNotificationMapper,
    SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper) {
    this.sendNotificationRepository = sendNotificationRepository;
    this.sendClient = sendClient;
    this.uploadService = uploadService;
    this.sendNotificationMapper = sendNotificationMapper;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
  }

  @Transactional
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
    List<PreLoadResponseDTO> preLoadResponseDTO = sendClient.preloadFiles(preLoadRequest, notification.getOrganizationId());
    preLoadResponseDTO.forEach(response ->
      sendNotificationRepository.updateFilePreloadInformation(sendNotificationId, response));

    sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.REGISTERED);
  }

  @Transactional
  @Override
  public void uploadFiles(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.REGISTERED, notification.getStatus());
    for(DocumentDTO doc : notification.getDocuments()){
      Optional<String> versionId = Optional.empty();
      if(!doc.getStatus().equals(FileStatus.UPLOADED))
        versionId = uploadService.uploadFile(notification.getOrganizationId(), sendNotificationId, doc);
      if (versionId.isPresent()) {
        sendNotificationRepository.updateFileStatus(sendNotificationId, doc.getFileName(), FileStatus.UPLOADED);
        sendNotificationRepository.updateFileVersionId(sendNotificationId, doc.getFileName(), versionId.get());
      }
    }
    sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.UPLOADED);
  }

  @Transactional
  @Override
  public void deliveryNotification(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.UPLOADED, notification.getStatus());
    NewNotificationResponseDTO responseDTO = sendClient.deliveryNotification(sendNotificationMapper.apply(notification), notification.getOrganizationId());
    if (responseDTO!=null){
      sendNotificationRepository.updateNotificationRequestId(sendNotificationId, responseDTO.getNotificationRequestId());
      sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
    }
  }

  @Transactional
  @Override
  public SendNotificationDTO retrieveNotificationData(String sendNotificationId, Long organizationId) {
    SendNotification notification = findSendNotification(sendNotificationId, organizationId);
    if(notification.getNotificationData()!=null)
      return sendNotificationDTOMapper.apply(notification);

    PagoPa payment = notification.getPayments().getFirst().getPagoPa();
    NotificationPriceResponseV23DTO notificationPriceResponseV23DTO =  sendClient.retrieveNotificationPrice(payment.getCreditorTaxId(), payment.getNoticeCode(), organizationId);

    if(notificationPriceResponseV23DTO.getNotificationViewDate()!=null) {
      notification.setNotificationData(notificationPriceResponseV23DTO.getNotificationViewDate()
        .toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());
      return sendNotificationDTOMapper.apply(notification);
    }

    return null;
  }

  @Override
  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String sendNotificationId) {
    SendNotification notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.COMPLETE, notification.getStatus());
    NewNotificationRequestStatusResponseV24DTO notificationStatus = sendClient.notificationStatus(notification.getNotificationRequestId(), notification.getOrganizationId());
    if(notificationStatus!=null && notificationStatus.getIun() != null)
      sendNotificationRepository.updateNotificationIun(sendNotificationId, notificationStatus.getIun());

    return notificationStatus;
  }

  private SendNotification findSendNotification(String sendNotificationId) {
    return sendNotificationRepository.findById(sendNotificationId)
      .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + sendNotificationId));
  }

  private SendNotification findSendNotification(String sendNotificationId, Long organizationId) {
    return sendNotificationRepository.findByIdAndOrganizationId(sendNotificationId, organizationId)
      .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + sendNotificationId));
  }
}
