package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SendFacadeServiceImpl implements SendFacadeService {
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final SendClient sendClient;
  private final SendUploadFacadeServiceImpl uploadService;
  private final SendNotification2NewNotificationRequestMapper sendNotificationMapper;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;

  public SendFacadeServiceImpl(
    SendNotificationNoPIIRepository sendNotificationNoPIIRepository,
                               SendClient sendClient, SendUploadFacadeServiceImpl uploadService,
                               SendNotification2NewNotificationRequestMapper sendNotificationMapper,
    SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper) {
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.sendClient = sendClient;
    this.uploadService = uploadService;
    this.sendNotificationMapper = sendNotificationMapper;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
  }

  @Transactional
  @Override
  public void preloadFiles(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

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
      sendNotificationNoPIIRepository.updateFilePreloadInformation(sendNotificationId, response));

    sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.REGISTERED);
  }

  @Transactional
  @Override
  public void uploadFiles(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.REGISTERED, notification.getStatus());
    for(DocumentDTO doc : notification.getDocuments()){
      Optional<String> versionId = Optional.empty();
      if(!doc.getStatus().equals(FileStatus.UPLOADED))
        versionId = uploadService.uploadFile(notification.getOrganizationId(), sendNotificationId, doc);
      if (versionId.isPresent()) {
        sendNotificationNoPIIRepository.updateFileStatus(sendNotificationId, doc.getFileName(), FileStatus.UPLOADED);
        sendNotificationNoPIIRepository.updateFileVersionId(sendNotificationId, doc.getFileName(), versionId.get());
      }
    }
    sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.UPLOADED);
  }

  @Transactional
  @Override
  public void deliveryNotification(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.UPLOADED, notification.getStatus());
    NewNotificationResponseDTO responseDTO = sendClient.deliveryNotification(sendNotificationMapper.apply(notification), notification.getOrganizationId());
    if (responseDTO!=null){
      sendNotificationNoPIIRepository.updateNotificationRequestId(sendNotificationId, responseDTO.getNotificationRequestId());
      sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
    }
  }

  @Transactional
  @Override
  public SendNotificationDTO retrieveNotificationData(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);
    if(notification.getNotificationData()!=null)
      return sendNotificationDTOMapper.apply(notification);

    PagoPa payment = notification.getPayments().getFirst().getPayment().getPagoPa();
    NotificationPriceResponseV23DTO notificationPriceResponseV23DTO =  sendClient.retrieveNotificationPrice(payment.getCreditorTaxId(), payment.getNoticeCode(), notification.getOrganizationId());

    if(notificationPriceResponseV23DTO.getNotificationViewDate()!=null) {
      notification.setNotificationData(notificationPriceResponseV23DTO.getNotificationViewDate()
        .toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());
      sendNotificationNoPIIRepository.updateNotificationDate(sendNotificationId, notification.getNotificationData());
      return sendNotificationDTOMapper.apply(notification);
    }

    return null;
  }

  @Override
  public SendNotificationDTO notificationStatus(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    if(!notification.getStatus().equals(NotificationStatus.COMPLETE) && !notification.getStatus().equals(NotificationStatus.ACCEPTED))
      NotificationUtils.validateStatus(NotificationStatus.COMPLETE, notification.getStatus());

    NewNotificationRequestStatusResponseV24DTO notificationStatus = sendClient.notificationStatus(notification.getNotificationRequestId(), notification.getOrganizationId());
    if(notification.getIun()==null && notificationStatus!=null && notificationStatus.getIun() != null){
      sendNotificationNoPIIRepository.updateNotificationIun(sendNotificationId, notificationStatus.getIun());
      notification.setIun(notificationStatus.getIun());
      notification.setStatus(NotificationStatus.ACCEPTED);
    }
    SendNotificationDTO sendNotificationDTO = sendNotificationDTOMapper.apply(notification);

    if(notificationStatus!=null && notificationStatus.getErrors()!=null)
     sendNotificationDTO.setErrors(notificationStatus.getErrors().stream()
       .map(ProblemErrorDTO::getDetail).toList());

    return sendNotificationDTO;
  }

  @Override
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav) {
    SendNotificationNoPII notification = findSendNotificationByOrgIdAndNav(organizationId, nav);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.ACCEPTED, notification.getStatus());
    Payment payment = notification.getPayments().stream()
      .map(PuPayment::getPayment)
      .filter(pagoPa -> nav.equals(pagoPa.getPagoPa().getNoticeCode()))
      .findFirst()
      .orElseThrow(() -> new NotFoundException("Notification not found with nav: "+ nav));

    return sendClient.retrieveNotificationPrice(payment.getPagoPa().getCreditorTaxId(),
      payment.getPagoPa().getNoticeCode(), notification.getOrganizationId());
  }

  private SendNotificationNoPII findSendNotification(String sendNotificationId) {
    return sendNotificationNoPIIRepository.findById(sendNotificationId)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with id: " + sendNotificationId));
  }

  private SendNotificationNoPII findSendNotificationByOrgIdAndNav(Long organizationId, String nav) {
    return sendNotificationNoPIIRepository.findByOrganizationIdAndNav(organizationId, nav)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with nav: " + nav));
  }
}
