package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeys;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdndservices.PdndService;
import it.gov.pagopa.pu.send.exception.common.NotFoundException;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;
import it.gov.pagopa.send.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.send.dto.generated.StreamCreationRequestV28DTO;
import it.gov.pagopa.send.dto.generated.StreamListElementDTO;
import it.gov.pagopa.send.dto.generated.StreamMetadataResponseV28DTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendStreamServiceImpl implements SendStreamService{

  private final SendClient client;
  private final OrganizationService organizationService;
  private final PdndService pdndService;

  public SendStreamServiceImpl(SendClient sendClient,
    OrganizationService organizationService, PdndService pdndService) {
    this.client = sendClient;
    this.organizationService = organizationService;
    this.pdndService = pdndService;
  }

  @Override
  public StreamMetadataResponseV28DTO createStream(StreamCreationRequestV28DTO createStreamRequest, Long organizationId, String subUnitCode, String accessToken) {
    return client.createStream(createStreamRequest, getApiKey(organizationId, subUnitCode, accessToken),
      pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public List<StreamListElementDTO> getStreams(Long organizationId, String subUnitCode, String accessToken) {
    return client.getStreams(getApiKey(organizationId, subUnitCode, accessToken), pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
  }

  @Override
  public List<ProgressResponseElementV28DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String subUnitCode, String accessToken) {
    return client.getStreamEvents(streamId, lastEventId, getApiKey(organizationId, subUnitCode, accessToken),
        pdndService.resolvePdndAccessToken(organizationId, subUnitCode, accessToken));
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
