package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdnd.PdndService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendServiceImpl implements SendService {

  private final SendClient client;
  private final OrganizationService organizationService;
  private final PdndService pdndService;

  public SendServiceImpl(SendClient client, OrganizationService organizationService,
    PdndService pdndService) {
    this.client = client;
    this.organizationService = organizationService;
    this.pdndService = pdndService;
  }

  @Override
  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, Long organizationId, String accessToken) {
    return client.preloadFiles(preLoadRequestDTO, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  @Override
  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, Long organizationId, String accessToken) {
    return client.deliveryNotification(newNotificationRequestV24DTO, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  @Override
  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, Long organizationId, String accessToken) {
    return client.notificationStatus(notificationRequestId, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  @Override
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, Long organizationId, String accessToken) {
    return client.retrieveNotificationPrice(paTaxId, noticeCode, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  @Override
  public List<LegalFactListElementV20DTO> getLegalFacts(String iun, Long organizationId, String accessToken) {
    return client.getLegalFacts(iun, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  private String getApiKeyFromOrganization(Long organizationId, String accessToken) {
    return organizationService.getOrganizationApiKey(organizationId, accessToken);
  }
}
