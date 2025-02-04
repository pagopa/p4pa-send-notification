package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.enums.Status;
import it.gov.pagopa.pu.send.model.DocumentDTO;
import it.gov.pagopa.pu.send.model.SendNotification;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CreateNotificationRequest2SendNotificationMapper {

  public SendNotification map(CreateNotificationRequest request) {
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSubjectType(request.getRecipient().getRecipientType().getValue());
    sendNotification.setFiscalCode(request.getRecipient().getTaxId());

    // set documents
    sendNotification.setDocuments(request.getDocuments().stream()
      .map(document ->
        DocumentDTO.builder()
        .fileName(document.getFileName())
        .contentType(document.getContentType())
        .digest(document.getDigest())
        .status(Status.WAITING_FILE)
        .build()).collect(Collectors.toList()));
    return sendNotification;
  }
}
