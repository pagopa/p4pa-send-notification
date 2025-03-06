package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.pagopa.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApisHolder;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestV24DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationResponseDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendClient {

  private final PagopaSendApisHolder apisHolder;
  private final OrganizationService organizationService;
  private static final String KEY_TYPE = "SEND";

  public SendClient(
    PagopaSendApisHolder apisHolder,
    OrganizationService organizationService
  ) {
    this.apisHolder = apisHolder;
    this.organizationService = organizationService;
  }

  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO) {
    String apiKey = getApiKeyFromOrganization();
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .presignedUploadRequest(preLoadRequestDTO);
  }

  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO) {
    String apiKey = getApiKeyFromOrganization();
    return apisHolder.getNewNotificationApiByApiKey(apiKey)
      .sendNewNotificationV24(newNotificationRequestV24DTO);
  }

  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId) {
    String apiKey = getApiKeyFromOrganization();
    return apisHolder.getSenderReadB2BApiByApiKey(apiKey)
      .retrieveNotificationRequestStatusV24(notificationRequestId, null, null);
  }

  private String getApiKeyFromOrganization() {
    String accessToken = SecurityUtils.getAccessToken();
    String orgIpaCode = SecurityUtils.getOrganizationIpaCode();
    Organization org = organizationService.getOrganizationByIpaCode(orgIpaCode, accessToken)
      .orElseThrow(() -> new NotFoundException("Organization not found with ipaCode: " + orgIpaCode));
    return organizationService.getOrganizationApiKey(org.getOrganizationId(), KEY_TYPE, accessToken);
  }

}
