package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.pagopa.send.SendService;
import it.gov.pagopa.pu.send.connector.pagopa.send.SendStreamService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamCreationRequestV28DTO.EventTypeEnum;
import it.gov.pagopa.pu.send.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.SendLegalFactMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2NewNotificationRequestMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.mapper.SendStreamMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.repository.SendStreamRepository;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import it.gov.pagopa.pu.send.util.HttpUtils;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class SendFacadeServiceImpl implements SendFacadeService {

  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final SendStreamRepository sendStreamRepository;
  private final SendService sendService;
  private final SendUploadFacadeServiceImpl uploadService;
  private final SendNotification2NewNotificationRequestMapper sendNotificationMapper;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;
  private final SendLegalFactMapper sendLegalFactMapper;
  private final SendStreamMapper sendStreamMapper;
  private final SendStreamService sendStreamService;
  private final WorkflowService workflowService;
  private final SendNotificationService sendNotificationService;

  private final CloseableHttpClient httpClient;

  public SendFacadeServiceImpl(
    SendNotificationNoPIIRepository sendNotificationNoPIIRepository,
    SendStreamRepository sendStreamRepository,
    SendService sendService,
    SendUploadFacadeServiceImpl uploadService,
    SendNotification2NewNotificationRequestMapper sendNotificationMapper,
    SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper,
    SendLegalFactMapper sendLegalFactMapper,
    SendStreamMapper sendStreamMapper,
    SendStreamService sendStreamService,
    WorkflowService workflowService,
    SendNotificationService sendNotificationService,
    CloseableHttpClient httpClient) {
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.sendStreamRepository = sendStreamRepository;
    this.sendService = sendService;
    this.uploadService = uploadService;
    this.sendNotificationMapper = sendNotificationMapper;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
    this.sendLegalFactMapper = sendLegalFactMapper;
    this.sendStreamMapper = sendStreamMapper;
    this.sendStreamService = sendStreamService;
    this.workflowService = workflowService;
    this.sendNotificationService = sendNotificationService;
    this.httpClient = httpClient;
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

    sendNotificationNoPIIRepository.updateNotificationStatusById(sendNotificationId, NotificationStatus.REGISTERED);
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
    sendNotificationNoPIIRepository.updateNotificationStatusById(sendNotificationId, NotificationStatus.UPLOADED);
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
        sendNotificationNoPIIRepository.updateNotificationStatusById(sendNotificationId, NotificationStatus.IN_VALIDATION);
      }
    }  catch (HttpClientErrorException.Conflict ex) {
      sendNotificationNoPIIRepository.updateNotificationStatusById(sendNotificationId, NotificationStatus.REFUSED);
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
    }
  }

  @Transactional
  @Override
  public SendNotificationDTO retrieveNotificationDate(String sendNotificationId, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    notification.getRecipients().forEach(puRecipientNoPIIDTO ->
      puRecipientNoPIIDTO.getPuPayments().forEach(puPayment -> {
        PagoPa payment = puPayment.getPayment().getPagoPa();
        if (payment != null) {
          NotificationPriceResponseV23DTO notificationPriceResponseV23DTO = sendService.retrieveNotificationPrice(payment.getCreditorTaxId(), payment.getNoticeCode(), notification.getOrganizationId(), accessToken);

          if (notificationPriceResponseV23DTO.getRefinementDate() != null) {
            puPayment.setNotificationDate(notificationPriceResponseV23DTO.getRefinementDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());
            sendNotificationNoPIIRepository.updateNotificationDate(sendNotificationId, puPayment.getNotificationDate(), puPayment.getPayment().getPagoPa().getNoticeCode());
          }
        }
      })
    );

    return sendNotificationDTOMapper.apply(notification);
  }

  @Override
  public SendNotificationDTO notificationStatus(String sendNotificationId, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    if (!notification.getStatus().equals(NotificationStatus.IN_VALIDATION) && !notification.getStatus().equals(NotificationStatus.ACCEPTED))
      NotificationUtils.validateStatus(NotificationStatus.IN_VALIDATION, notification.getStatus());

    NewNotificationRequestStatusResponseV25DTO notificationStatus = sendService.notificationStatus(notification.getNotificationRequestId(), notification.getOrganizationId(), accessToken);
    if (notification.getIun() == null && notificationStatus != null && notificationStatus.getIun() != null) {
      sendNotificationNoPIIRepository.updateNotificationIun(sendNotificationId, notificationStatus.getIun());
      notification.setIun(notificationStatus.getIun());
      notification.setStatus(NotificationStatus.ACCEPTED);
    }
    SendNotificationDTO sendNotificationDTO = sendNotificationDTOMapper.apply(notification);

    if (notificationStatus != null && notificationStatus.getErrors() != null && !notificationStatus.getErrors().isEmpty()) {
      sendNotificationDTO.setErrors(notificationStatus.getErrors().stream()
        .map(NotificationRequestRefusedProblemErrorDTO::getDetail).toList());
      sendNotificationNoPIIRepository.updateNotificationStatusById(sendNotificationId, NotificationStatus.REFUSED);
      sendNotificationDTO.setStatus(NotificationStatus.REFUSED);
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
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_NOTIFICATION_NOT_FOUND, "Notification not found with nav: " + nav));

    return sendService.retrieveNotificationPrice(payment.getPagoPa().getCreditorTaxId(),
      payment.getPagoPa().getNoticeCode(), notification.getOrganizationId(), accessToken);
  }

  @Override
  public List<ProgressResponseElementV28DTO> getStreamEvents(String streamId, Long organizationId, String accessToken) {
    if (StringUtils.isBlank(streamId)) {
      List<StreamListElementDTO> streams = sendStreamService.getStreams(organizationId, accessToken);
      if (streams.isEmpty())
        throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_STREAMS_NOT_FOUND, "Streams not found for this organization: " + organizationId);

      streamId = String.valueOf(streams.getLast().getStreamId());
    }
    SendStreamDTO stream = this.getStream(streamId, accessToken); //for fetching lastEventId from cache
    return sendStreamService.getStreamEvents(streamId, stream.getLastEventId(), organizationId, accessToken);
  }

  @Override
  public void updateStreamLastEventId(String streamId, String lastEventId) {
    sendStreamRepository.updateLastEventId(streamId, lastEventId);
  }

  @Override
  public SendStreamDTO getStream(String streamId, String accessToken) {
    Optional<SendStream> sendStream = sendStreamRepository.findById(streamId);
    if (sendStream.isEmpty() || !cachedStreamDoesExistOnSend(streamId, sendStream.get().getOrganizationId(), accessToken)) {
      sendStreamRepository.deleteById(streamId);
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_STREAMS_NOT_FOUND, String.format("Send stream not found for streamId: %s", streamId));
    }
    return sendStreamMapper.mapToSendStreamDTO(sendStream.get());
  }

  private boolean cachedStreamDoesExistOnSend(String streamId, Long organizationId, String accessToken) {
    return sendStreamService.getStreams(organizationId, accessToken).stream()
      .map(StreamListElementDTO::getStreamId)
      .filter(Objects::nonNull)
      .anyMatch(s -> s.toString().equals(streamId));
  }

  @Override
  public List<LegalFactListElementDTO> retrieveLegalFacts(String sendNotificationId, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    // Validate status
    if(!NotificationStatus.ACCEPTED.equals(notification.getStatus())){
      throw new InvalidStatusException(ErrorCodeConstants.ERROR_CODE_INVALID_NOTIFICATION_STATUS, NotificationStatus.ACCEPTED, notification.getStatus());
    }

    return sendService.getLegalFacts(notification.getIun(), notification.getOrganizationId(), accessToken)
      .stream()
      .map(sendLegalFactMapper::mapLegalFactDTOFromSend)
      .toList();
  }

  @Override
  public LegalFactDownloadMetadataDTO retrieveLegalFactDownloadMetadata(String sendNotificationId,
                                                                        String legalFactId,
                                                                        String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);

    return this.retrieveLegalFactDownloadMetadata(sendNotificationDTOMapper.apply(notification), legalFactId, accessToken);
  }

  private LegalFactDownloadMetadataDTO retrieveLegalFactDownloadMetadata(SendNotificationDTO sendNotification,
                                                                         String legalFactId,
                                                                        String accessToken) {
    LegalFactDownloadMetadataResponseDTO legalFactDownloadMetadata = sendService.getLegalFactDownloadMetadata(
      sendNotification.getIun(),
      legalFactId,
      sendNotification.getOrganizationId(),
      accessToken
    );

    return sendLegalFactMapper.mapLegalFactDownloadMetadataFromSend(legalFactDownloadMetadata);
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
    StreamCreationRequestV28DTO request = new StreamCreationRequestV28DTO();
    request.setTitle("SEND-STREAM_" + organizationId);
    request.setEventType(EventTypeEnum.TIMELINE);

    List<SendStream> sendStreamList = sendStreamRepository.findByOrganizationId(organizationId);
    if(sendStreamList.isEmpty()) {
      StreamMetadataResponseV28DTO streamMetadataResponseV28DTO =
        sendStreamService.createStream(request, organizationId, accessToken);
      sendStreamRepository.save(sendStreamMapper.mapToSendStream(streamMetadataResponseV28DTO, organizationId));
      workflowService.sendNotificationStreamConsume(
        streamMetadataResponseV28DTO.getStreamId().toString(),
        accessToken
      );
    }
  }

  @Override
  public void downloadAndArchiveSendLegalFact(String notificationRequestId, LegalFactCategoryDTO category, String legalFactId, String accessToken) throws IOException {
    SendNotificationDTO sendNotificationDTO = sendNotificationService.findSendNotificationDTOByNotificationRequestId(notificationRequestId);
    if(sendNotificationDTO == null) {
      String formattedErrorMessage = "Error in fetching SEND notification by notificationRequestId %s".formatted(notificationRequestId);
      throw new SendNotificationNotFoundException(formattedErrorMessage);
    }
    String sendNotificationId = sendNotificationDTO.getSendNotificationId();
    String polishedLegalFactId = sendLegalFactMapper.polishLegalFactIdKey(legalFactId);
    LegalFactDownloadMetadataDTO legalFactDownloadMetadataDTO =
      this.retrieveLegalFactDownloadMetadata(
        sendNotificationDTO,
        polishedLegalFactId,
        accessToken
      );
    if(legalFactDownloadMetadataDTO == null || legalFactDownloadMetadataDTO.getUrl() == null) {
      String formattedErrorMessage = "Error in fetching SEND LegalFact pre-signed URL for sendNotificationDTO %s, category %s, legalFactId %s"
        .formatted(sendNotificationId, category.getValue(), polishedLegalFactId);
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_LEGAL_FACT_URL_NOT_FOUND, formattedErrorMessage);
    }
    String preSignedUrl = legalFactDownloadMetadataDTO.getUrl();

    byte[] bytes = HttpUtils.downloadFromPreSignedUrl(URI.create(preSignedUrl), httpClient);
    try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
      sendNotificationService.uploadSendLegalFact(sendNotificationId, category, polishedLegalFactId, inputStream);
    }
  }

}
