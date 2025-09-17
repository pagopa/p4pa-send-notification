package it.gov.pagopa.pu.send.service;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.DeleteFileException;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.SendNotificationFileNotFoundException;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import it.gov.pagopa.pu.send.repository.SendNotificationPIIRepository;
import it.gov.pagopa.pu.send.util.AESUtils;
import it.gov.pagopa.pu.send.util.FileUtils;
import it.gov.pagopa.pu.send.util.NotificationUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationPIIRepository sendNotificationPIIRepository;
  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final WorkflowService workflowService;
  private final CreateNotificationRequest2SendNotificationMapper mapper;
  private final String fileShareBaseUrl;
  private final FileRetrieverService fileRetrieverService;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;

  public SendNotificationServiceImpl(@Value("${fileshare-public-base-url}") String fileShareBaseUrl,
                                     SendNotificationPIIRepository sendNotificationPIIRepository,
    SendNotificationNoPIIRepository sendNotificationNoPIIRepository, CreateNotificationRequest2SendNotificationMapper mapper, WorkflowService workflowService,
                                     FileRetrieverService fileRetrieverService, SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper) {
    this.fileShareBaseUrl = fileShareBaseUrl;
    this.sendNotificationPIIRepository = sendNotificationPIIRepository;
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.mapper = mapper;
    this.workflowService = workflowService;
    this.fileRetrieverService = fileRetrieverService;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
  }

  @Transactional
  @Override
  public CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest, String accessToken) {
    SendNotification sendNotification = sendNotificationPIIRepository.save(mapper.mapToModel(createNotificationRequest, accessToken));

    return CreateNotificationResponse
      .builder()
      .sendNotificationId(sendNotification.getSendNotificationId())
      .status(sendNotification.getStatus().name())
      .preloadUrl(fileShareBaseUrl+"/organization/"+ sendNotification.getOrganizationId()+"/send-files/"+ sendNotification.getSendNotificationId())
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
      sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, NotificationStatus.SENDING);
      WorkflowCreatedDTO workflow = workflowService.sendNotificationProcess(sendNotificationId, accessToken);
      return StartNotificationResponse.builder()
        .workflowId(workflow.getWorkflowId())
        .runId(workflow.getRunId())
        .build();
    }
    return null;
  }

  @Transactional
  @Override
  public void deleteSendNotification(String sendNotificationId) {
    SendNotificationNoPII notification = findSendNotification(sendNotificationId);
    if (!notification.getStatus().equals(NotificationStatus.COMPLETE)
      && !notification.getStatus().equals(NotificationStatus.ACCEPTED)) {
      sendNotificationNoPIIRepository.deleteById(sendNotificationId);
      deleteSendNotificationFiles(notification);
    }
    else
      throw new InvalidStatusException("Cannot delete notification with status complete");
  }

  @Override
  public SendNotificationDTO findSendNotificationDTO(String sendNotificationId) {
    return sendNotificationDTOMapper.apply(findSendNotification(sendNotificationId));
  }

  @Override
  public SendNotificationDTO findSendNotificationByOrgIdAndNav(Long organizationId, String nav) {
    return sendNotificationDTOMapper.apply(sendNotificationNoPIIRepository.findByOrganizationIdAndNav(organizationId, nav)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with orgId "+organizationId+" and nav " + nav)));
  }

  @Override
  public UpdateResult updateNotificationStatus(String sendNotificationId, NotificationStatus newStatus) {
    return sendNotificationNoPIIRepository.updateNotificationStatus(sendNotificationId, newStatus);
  }

  private SendNotificationNoPII findSendNotification(String sendNotificationId) {
    return sendNotificationNoPIIRepository.findById(sendNotificationId)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with id: " + sendNotificationId));
  }


  private void updateFileStatus(String sendNotificationId, DocumentDTO doc, LoadFileRequest loadFileRequest, Long organizationId) {
    NotificationUtils.validateStatus(FileStatus.WAITING, doc.getStatus());
    try {
      String fileName = sendNotificationId +"_" + doc.getFileName();
      InputStream file = fileRetrieverService.retrieveFile(organizationId, sendNotificationId, fileName);
      if (!FileUtils.calculateFileHash(file).equals(loadFileRequest.getDigest()))
        throw new InvalidSignatureException("File " + doc.getFileName() + " has not a valid signature");
    } catch (Exception e) {
      throw new InvalidSignatureException(e.getMessage());
    }
    sendNotificationNoPIIRepository.updateFileStatus(sendNotificationId, doc.getFileName(), FileStatus.READY);
  }


  private void deleteSendNotificationFiles(SendNotificationNoPII sendNotification) {
    Path relativePath = fileRetrieverService.buildRelativeSendPath(
      sendNotification.getOrganizationId(),
      sendNotification.getSendNotificationId()
    );

    // Delete documents
    sendNotification.getDocuments().forEach(documentDTO ->
      deleteFile(relativePath, documentDTO.getFileName(), sendNotification.getSendNotificationId()));

    // Delete attachments
    sendNotification.getRecipients().forEach(recipient -> {
      List<PuPayment> puPayments = recipient.getPuPayments();
      if(puPayments!=null) {
        recipient.getPuPayments().forEach(puPayment -> {
          Optional.ofNullable(puPayment.getPayment().getPagoPa())
            .map(PagoPa::getAttachment)
            .ifPresent(attachment ->
              deleteFile(relativePath, attachment.getFileName(),
                sendNotification.getSendNotificationId())
            );

          Optional.ofNullable(puPayment.getPayment().getF24())
            .map(F24Payment::getMetadataAttachment)
            .ifPresent(attachment ->
              deleteFile(relativePath, attachment.getFileName(),
                sendNotification.getSendNotificationId())
            );
        });
      }
    });
  }

  private void deleteFile(Path basePath, String fileName, String sendNotificationId) {
    Path filePath = basePath.resolve(sendNotificationId + "_" + fileName + AESUtils.CIPHER_EXTENSION);
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new DeleteFileException(String.format(
        "Error while deleting file %s for sendNotificationId %s.", filePath.getFileName(), sendNotificationId
      ));
    }
  }

}
