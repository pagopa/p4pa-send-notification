package it.gov.pagopa.pu.send.connector.pagopa.send;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.send.connector.pagopa.send.client.SendClient;
import it.gov.pagopa.pu.send.connector.pdnd.PdndService;
import it.gov.pagopa.pu.send.connector.send.generated.dto.*;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendStreamServiceImplTest {

  @Mock
  private SendClient clientMock;
  @Mock
  private PdndService pdndServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private SendStreamService service;

  private final String accessToken = "ACCESSTOKEN";
  private final String voucherToken = "VOUCHERTOKEN";

  @BeforeEach
  void init() {
    service = new SendStreamServiceImpl(clientMock, organizationServiceMock, pdndServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(clientMock, organizationServiceMock, pdndServiceMock);
  }

  @Test
  void whenCreateStreamThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);

    StreamCreationRequestV28DTO request = new StreamCreationRequestV28DTO();
    StreamMetadataResponseV28DTO expectedResult = new StreamMetadataResponseV28DTO();

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    Mockito.when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    Mockito.when(clientMock.createStream(request, orgSendApiKey, voucherToken)).thenReturn(expectedResult);

    //When
    StreamMetadataResponseV28DTO result = service.createStream(request, organizationId, accessToken);

    //Then
    assertSame(expectedResult, result);
  }

  @Test
  void whenGetStreamsThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";

    List<StreamListElementDTO> expectedResult = List.of();

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    Mockito.when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    Mockito.when(clientMock.getStreams(orgSendApiKey, voucherToken)).thenReturn(expectedResult);

    //When
    List<StreamListElementDTO> result = service.getStreams(organizationId, accessToken);

    //Then
    assertSame(expectedResult, result);
  }

  @Test
  void whenGetStreamEventsThenInvokeClient() {
    // Given
    long organizationId = 123L;
    String orgSendApiKey = "ORG_SEND_API_KEY";
    String streamId = "streamId";

    List<ProgressResponseElementV28DTO> expectedResult = List.of();

    Mockito.when(organizationServiceMock.getOrganizationApiKey(organizationId, accessToken))
      .thenReturn(orgSendApiKey);
    Mockito.when(pdndServiceMock.resolvePdndAccessToken(organizationId, accessToken)).thenReturn(voucherToken);
    Mockito.when(clientMock.getStreamEvents(streamId, null, orgSendApiKey, voucherToken))
      .thenReturn(expectedResult);

    //When
    List<ProgressResponseElementV28DTO> result = service.getStreamEvents(streamId, null,organizationId, accessToken);

    //Then
    assertSame(expectedResult, result);
  }
}
