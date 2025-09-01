package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.SendService;
import it.gov.pagopa.pu.send.connector.pagopa.send.SendStreamService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamCreationRequestV25DTO.EventTypeEnum;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SendFacadeServiceImpl implements SendFacadeService {
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final SendService sendService;
  private final SendUploadFacadeServiceImpl uploadService;
  private final SendNotification2NewNotificationRequestMapper sendNotificationMapper;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;
  private final SendStreamService sendStreamService;

  public SendFacadeServiceImpl(
    SendNotificationNoPIIRepository sendNotificationNoPIIRepository,
    SendService sendService,
    SendUploadFacadeServiceImpl uploadService,
    SendNotification2NewNotificationRequestMapper sendNotificationMapper,
    SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper,
    SendStreamService sendStreamService) {
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.sendService = sendService;
    this.uploadService = uploadService;
    this.sendNotificationMapper = sendNotificationMapper;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
    this.sendStreamService = sendStreamService;
  }

  @Transactional
  @Override
  public void preloadFiles(String sendNotificationId, String accessToken) {
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
    List<PreLoadResponseDTO> preLoadResponseDTO = sendService.preloadFiles(preLoadRequest, notification.getOrganizationId(), accessToken);
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
    for (DocumentDTO doc : notification.getDocuments()) {
      Optional<String> versionId = Optional.empty();
      if (!doc.getStatus().equals(FileStatus.UPLOADED))
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
  public void deliveryNotification(String sendNotificationId, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.UPLOADED, notification.getStatus());
    //create stream if not already exists in cache
    createStream(notification.getOrganizationId(), accessToken);

    try {
      NewNotificationResponseDTO responseDTO = sendService.deliveryNotification(sendNotificationMapper.apply(notification), notification.getOrganizationId(), accessToken);
      if (responseDTO != null) {
        sendNotificationNoPIIRepository.updateNotificationRequestId(sendNotificationId, responseDTO.getNotificationRequestId());
        sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.COMPLETE);
      }
    } catch (HttpClientErrorException.Conflict ex) {
      sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.ERROR);
    }
  }

  @Transactional
  @Override
  public SendNotificationDTO retrieveNotificationDate(String sendNotificationId, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    notification.getRecipients().forEach(puRecipientNoPIIDTO ->
      puRecipientNoPIIDTO.getPuPayments().forEach(puPayment -> {
        PagoPa payment = puPayment.getPayment().getPagoPa();
        NotificationPriceResponseV23DTO notificationPriceResponseV23DTO = sendService.retrieveNotificationPrice(payment.getCreditorTaxId(), payment.getNoticeCode(), notification.getOrganizationId(), accessToken);

        if (notificationPriceResponseV23DTO.getNotificationViewDate() != null) {
          puPayment.setNotificationDate(notificationPriceResponseV23DTO.getNotificationViewDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());
          sendNotificationNoPIIRepository.updateNotificationDate(sendNotificationId, puPayment.getNotificationDate(), puPayment.getPayment().getPagoPa().getNoticeCode());
        }
      })
    );

    return sendNotificationDTOMapper.apply(notification);
  }

  @Override
  public SendNotificationDTO notificationStatus(String sendNotificationId, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    if (!notification.getStatus().equals(NotificationStatus.COMPLETE) && !notification.getStatus().equals(NotificationStatus.ACCEPTED))
      NotificationUtils.validateStatus(NotificationStatus.COMPLETE, notification.getStatus());

    NewNotificationRequestStatusResponseV24DTO notificationStatus = sendService.notificationStatus(notification.getNotificationRequestId(), notification.getOrganizationId(), accessToken);
    if (notification.getIun() == null && notificationStatus != null && notificationStatus.getIun() != null) {
      sendNotificationNoPIIRepository.updateNotificationIun(sendNotificationId, notificationStatus.getIun());
      notification.setIun(notificationStatus.getIun());
      notification.setStatus(NotificationStatus.ACCEPTED);
    }
    SendNotificationDTO sendNotificationDTO = sendNotificationDTOMapper.apply(notification);

    if (notificationStatus != null && notificationStatus.getErrors() != null && !notificationStatus.getErrors().isEmpty()) {
      sendNotificationDTO.setErrors(notificationStatus.getErrors().stream()
        .map(ProblemErrorDTO::getDetail).toList());
      sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.ERROR);
      sendNotificationDTO.setStatus(NotificationStatus.ERROR);
    }

    return sendNotificationDTO;
  }

  @Override
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String nav, String accessToken) {
    SendNotificationNoPII notification = findSendNotificationByOrgIdAndNav(organizationId, nav);

    // Validate status
    NotificationUtils.validateStatus(NotificationStatus.ACCEPTED, notification.getStatus());
    Payment payment = notification.getRecipients().stream()
      .flatMap(recipient -> recipient.getPuPayments().stream())
      .map(PuPayment::getPayment)
      .filter(pagoPa -> nav.equals(pagoPa.getPagoPa().getNoticeCode()))
      .findFirst()
      .orElseThrow(() -> new NotFoundException("Notification not found with nav: " + nav));

    return sendService.retrieveNotificationPrice(payment.getPagoPa().getCreditorTaxId(),
      payment.getPagoPa().getNoticeCode(), notification.getOrganizationId(), accessToken);
  }

  @Override
  public List<ProgressResponseElementV25DTO> getStreamEvents(String streamId, String lastEventId,
                                                             Long organizationId, String accessToken) {
    if (StringUtils.isBlank(streamId)) {
      List<StreamListElementDTO> streams = sendStreamService.getStreams(organizationId, accessToken);
      if (streams.isEmpty())
        throw new NotFoundException("Streams not found for this organization: " + organizationId);

      streamId = String.valueOf(streams.getLast().getStreamId());
    }

    return sendStreamService.getStreamEvents(streamId, lastEventId, organizationId, accessToken);
  }

  private SendNotificationNoPII findSendNotification(String sendNotificationId) {
    return sendNotificationNoPIIRepository.findById(sendNotificationId)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with id: " + sendNotificationId));
  }

  private SendNotificationNoPII findSendNotificationByOrgIdAndNav(Long organizationId, String nav) {
    return sendNotificationNoPIIRepository.findByOrganizationIdAndNav(organizationId, nav)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with nav: " + nav));
  }

  private void createStream(Long organizationId, String accessToken) {
    StreamCreationRequestV25DTO request = new StreamCreationRequestV25DTO();
    request.setTitle("SEND-STREAM_" + organizationId);
    request.setEventType(EventTypeEnum.STATUS);

    sendStreamService.createStream(request, organizationId, accessToken);
  }
}
