package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdnd.PdndService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;

import java.util.List;
import org.springframework.stereotype.Service;

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
  public StreamMetadataResponseV28DTO createStream(StreamCreationRequestV28DTO createStreamRequest, Long organizationId, String accessToken) {
    return client.createStream(createStreamRequest, getApiKeyFromOrganization(organizationId, accessToken),
      pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  @Override
  public List<StreamListElementDTO> getStreams(Long organizationId, String accessToken) {
    return client.getStreams(getApiKeyFromOrganization(organizationId, accessToken), pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  @Override
  public List<ProgressResponseElementV28DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String accessToken) {
    return client.getStreamEvents(streamId, lastEventId, getApiKeyFromOrganization(organizationId, accessToken),
        pdndService.resolvePdndAccessToken(organizationId, accessToken));
  }

  private String getApiKeyFromOrganization(Long organizationId, String accessToken) {
    return organizationService.getOrganizationApiKey(organizationId, accessToken);
  }

}
