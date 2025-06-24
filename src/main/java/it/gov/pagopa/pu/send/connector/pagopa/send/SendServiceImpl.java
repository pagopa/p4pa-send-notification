package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdnd.client.PdndApiClient;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendServiceImpl implements SendService {

  private final SendClient client;
  private final PdndApiClient pdndApiClient;
  private final OrganizationService organizationService;

  private final Map<String, String> pdndTokenHolder = new ConcurrentHashMap<>();

  public SendServiceImpl(SendClient client, PdndApiClient pdndApiClient, OrganizationService organizationService) {
    this.client = client;
    this.pdndApiClient = pdndApiClient;
    this.organizationService = organizationService;
  }

  private String getPdndAccessToken(String accessToken) {
    return pdndTokenHolder.computeIfAbsent(accessToken, voucher -> pdndApiClient.getVoucherToken(accessToken).getAccessToken());
  }

  @Override
  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, Long organizationId, String accessToken) {
    return client.preloadFiles(preLoadRequestDTO, getApiKeyFromOrganization(organizationId, accessToken), getPdndAccessToken(accessToken));
  }

  @Override
  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV24DTO newNotificationRequestV24DTO, Long organizationId, String accessToken) {
    return client.deliveryNotification(newNotificationRequestV24DTO, getApiKeyFromOrganization(organizationId, accessToken), getPdndAccessToken(accessToken));
  }

  @Override
  public NewNotificationRequestStatusResponseV24DTO notificationStatus(String notificationRequestId, Long organizationId, String accessToken) {
    return client.notificationStatus(notificationRequestId, getApiKeyFromOrganization(organizationId, accessToken), getPdndAccessToken(accessToken));
  }

  @Override
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, Long organizationId, String accessToken) {
    return client.retrieveNotificationPrice(paTaxId, noticeCode, getApiKeyFromOrganization(organizationId, accessToken), getPdndAccessToken(accessToken));
  }

  private String getApiKeyFromOrganization(Long organizationId, String accessToken) {
    return organizationService.getOrganizationApiKey(organizationId, accessToken);
  }
}
