package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.SendNotification;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationMapper {

  public SendNotification apply(CreateNotificationRequest createNotificationRequest) {
    return SendNotification.builder()
      .preloadId(createNotificationRequest.getPreloadId())
      .contentType(createNotificationRequest.getContentType())
      .expectedFileDigest(createNotificationRequest.getSha256())
      .build();
  }
}
