package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;

import java.util.List;

public interface SendService {
  List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, Long organizationId, String accessToken);
  NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, Long organizationId, String accessToken);
  NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, Long organizationId, String accessToken);
  NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, Long organizationId, String accessToken);
}
