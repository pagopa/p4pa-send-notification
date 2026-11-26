package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeys;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdndservices.PdndService;
import it.gov.pagopa.pu.send.exception.common.NotFoundException;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import it.gov.pagopa.send.dto.generated.*;
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
  public List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO, Long organizationId, String subUnitCode, String accessToken) {
    return client.preloadFiles(preLoadRequestDTO, getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public NewNotificationResponseDTO deliveryNotification(NewNotificationRequestV25DTO newNotificationRequestV25DTO, Long organizationId, String subUnitCode, String accessToken) {
    return client.deliveryNotification(newNotificationRequestV25DTO, getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public NewNotificationRequestStatusResponseV25DTO notificationStatus(String notificationRequestId, Long organizationId, String subUnitCode, String accessToken) {
    return client.notificationStatus(notificationRequestId, getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public NotificationPriceResponseV23DTO retrieveNotificationPrice(String paTaxId, String noticeCode, Long organizationId, String subUnitCode, String accessToken) {
    return client.retrieveNotificationPrice(paTaxId, noticeCode, getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public List<LegalFactListElementV20DTO> getLegalFacts(String iun, Long organizationId, String subUnitCode, String accessToken) {
    return client.getLegalFacts(iun, getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public LegalFactDownloadMetadataResponseDTO getLegalFactDownloadMetadata(String iun, String legalFactId, Long organizationId, String subUnitCode, String accessToken) {
    return client.getLegalFactDownloadMetadata(iun, legalFactId, getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  private String getApiKey(Long organizationId, String subUnitCode, String accessToken) {
    OrganizationApiKeys organizationApiKeys = organizationService.getOrganizationApiKey(organizationId, subUnitCode, accessToken);
    if (organizationApiKeys == null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_SEND_API_KEY_NOT_FOUND,
        String.format("SEND api key not found for organizationId %s and subUnitCode %s", organizationId, subUnitCode));
    }
    return organizationApiKeys.getApiKey();
  }
}
