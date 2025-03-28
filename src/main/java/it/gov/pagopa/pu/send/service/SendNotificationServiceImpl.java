package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.citizen.model.PersonalData;
import it.gov.pagopa.pu.send.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.SendNotificationFileNotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import it.gov.pagopa.pu.send.util.FileUtils;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationRepository sendNotificationRepository;
  private final WorkflowService workflowService;
  private final CreateNotificationRequest2SendNotificationMapper mapper;
  private final String fileShareBaseUrl;
  private final FileRetrieverService fileRetrieverService;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;
  private final PersonalDataRepository personalDataRepository;
  private final DataCipherService dataCipherService;

  public SendNotificationServiceImpl(@Value("${fileshare-public-base-url}") String fileShareBaseUrl,
                                     SendNotificationRepository sendNotificationRepository, CreateNotificationRequest2SendNotificationMapper mapper, WorkflowService workflowService,
                                     FileRetrieverService fileRetrieverService, SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper,
    PersonalDataRepository personalDataRepository,
    DataCipherService dataCipherService) {
    this.fileShareBaseUrl = fileShareBaseUrl;
    this.sendNotificationRepository = sendNotificationRepository;
    this.mapper = mapper;
    this.workflowService = workflowService;
    this.fileRetrieverService = fileRetrieverService;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
    this.personalDataRepository = personalDataRepository;
    this.dataCipherService = dataCipherService;
  }

  @Transactional
  @Override
  public CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest, String accessToken) {
    SendNotificationNoPII noPII = mapper.mapToNoPII(createNotificationRequest, accessToken);
    SendNotificationPIIDTO pii = mapper.mapToPii(createNotificationRequest);

    Long personalDataId = personalDataRepository.save(PersonalData.builder()
      .type("SEND_NOTIFICATION")
      .data(dataCipherService.encryptObj(pii)).build()).getId();
    noPII.setPersonalDataId(personalDataId);
    SendNotificationNoPII sendNotificationNoPII = sendNotificationRepository.insert(noPII);

    return CreateNotificationResponse
      .builder()
      .sendNotificationId(sendNotificationNoPII.getSendNotificationId())
      .status(sendNotificationNoPII.getStatus().name())
      .preloadUrl(fileShareBaseUrl+"/organization/"+ sendNotificationNoPII.getOrganizationId()+"/send-files/"+ sendNotificationNoPII.getSendNotificationId())
      .build();
  }

  @Transactional
  @Override
  public StartNotificationResponse startSendNotification(String sendNotificationId, LoadFileRequest loadFileRequest, String accessToken) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);
    NotificationUtils.validateStatus(NotificationStatus.WAITING_FILE, notification.getStatus());

    notification.getDocuments().stream()
      .filter(doc -> doc.getFileName().equals(loadFileRequest.getFileName()))
      .findFirst().ifPresentOrElse(
        doc -> updateFileStatus(sendNotificationId, doc, loadFileRequest, notification.getOrganizationId()),
        () -> {
          throw new SendNotificationFileNotFoundException("File not found with id: " + loadFileRequest.getFileName());
        }
      );

    SendNotificationNoPII updatedNotification = findSendNotification(sendNotificationId);
    boolean allFilesReady = updatedNotification.getDocuments().stream()
      .allMatch(doc -> doc.getStatus().equals(FileStatus.READY));

    if (allFilesReady) {
      sendNotificationRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.SENDING);
      WorkflowCreatedDTO workflow = workflowService.sendNotificationProcess(sendNotificationId, accessToken);
      return StartNotificationResponse.builder()
        .workFlowId(workflow.getWorkflowId())
        .build();
    }
    return null;
  }

  @Transactional
  @Override
  public void deleteSendNotification(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);
    if (!notification.getStatus().equals(NotificationStatus.COMPLETE))
      sendNotificationRepository.deleteById(sendNotificationId);
    else
      throw new InvalidStatusException("Cannot delete notification with status complete");
  }

  @Override
  public SendNotificationDTO findSendNotificationDTO(String sendNotificationId) {
    return sendNotificationDTOMapper.apply(findSendNotification(sendNotificationId));
  }

  private SendNotificationNoPII findSendNotification(String sendNotificationId) {
    return sendNotificationRepository.findById(sendNotificationId)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with id: " + sendNotificationId));
  }

  private void updateFileStatus(String sendNotificationId, DocumentDTO doc, LoadFileRequest loadFileRequest, Long organizationId) {
    NotificationUtils.validateStatus(FileStatus.WAITING, doc.getStatus());
    try {
      String fileName = sendNotificationId +"_" + doc.getFileName();
      InputStream file = fileRetrieverService.retrieveFile(organizationId, fileName);
      if (!FileUtils.calculateFileHash(file).equals(loadFileRequest.getDigest()))
        throw new InvalidSignatureException("File " + doc.getFileName() + " has not a valid signature");
    } catch (Exception e) {
      throw new InvalidSignatureException(e.getMessage());
    }
    sendNotificationRepository.updateFileStatus(sendNotificationId, doc.getFileName(), FileStatus.READY);
  }
}
