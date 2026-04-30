package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.send.generated.dto.*;

import java.util.List;

public interface SendStreamService {
  StreamMetadataResponseV28DTO createStream(StreamCreationRequestV28DTO createStreamRequest, Long organizationId, String accessToken);
  List<StreamListElementDTO> getStreams(Long organizationId, String accessToken);
  List<ProgressResponseElementV28DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String accessToken);
}
