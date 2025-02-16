package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import java.util.List;

public interface SendService {
  List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO);
  NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO);
}
