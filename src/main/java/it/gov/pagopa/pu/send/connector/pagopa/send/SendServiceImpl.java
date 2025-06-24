package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdnd.PdndService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
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
    return client.preloadFiles(preLoadRequestDTO, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(accessToken));
  }

  @Override
  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, Long organizationId, String accessToken) {
    return client.deliveryNotification(newNotificationRequestV24DTO, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(accessToken));
  }

  @Override
  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, Long organizationId, String accessToken) {
    return client.notificationStatus(notificationRequestId, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(accessToken));
  }

  @Override
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, Long organizationId, String accessToken) {
    return client.retrieveNotificationPrice(paTaxId, noticeCode, getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(accessToken));
  }

  private String getApiKeyFromOrganization(Long organizationId, String accessToken) {
    String apiKey = organizationService.getOrganizationApiKey(organizationId, accessToken);
      log.info("ORGID {}, APIKEY {}", organizationId, apiKey);
    return apiKey;
  }
}
