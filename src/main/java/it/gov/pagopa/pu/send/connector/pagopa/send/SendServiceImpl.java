package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendServiceImpl implements SendService {

  private final SendClient client;

  public SendServiceImpl(SendClient client) {
    this.client = client;
  }

  @Override
  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, Long organizationId) {
    return client.preloadFiles(preLoadRequestDTO, organizationId);
  }

  @Override
  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, Long organizationId) {
    return client.deliveryNotification(newNotificationRequestV24DTO, organizationId);
  }
}
