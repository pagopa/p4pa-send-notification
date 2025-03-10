package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.connector.pagopa.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;
  private final OrganizationService organizationService;

  public SendClient(
    PagopaSendApisHolder apisHolder,
    OrganizationService organizationService
  ) {
    this.apisHolder = apisHolder;
    this.organizationService = organizationService;
  }

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, Long organizationId) {
    String apiKey = getApiKeyFromOrganization(organizationId);
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, Long organizationId) {
    String apiKey = getApiKeyFromOrganization(organizationId);
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .sendNewNotificationV24(newNotificationRequestV24DTO);
  }

  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, Long organizationId) {
    String apiKey = getApiKeyFromOrganization(organizationId);
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey)
      .retrieveNotificationRequestStatusV24(notificationRequestId, null, null);
  }

  private String getApiKeyFromOrganization(Long organizationId) {
    return organizationService.getOrganizationApiKey(organizationId);
  }

}
