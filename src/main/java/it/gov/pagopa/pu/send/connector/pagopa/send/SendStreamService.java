package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.send.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.send.dto.generated.StreamCreationRequestV28DTO;
import it.gov.pagopa.send.dto.generated.StreamListElementDTO;
import it.gov.pagopa.send.dto.generated.StreamMetadataResponseV28DTO;

import java.util.List;

public interface SendStreamService {
  StreamMetadataResponseV28DTO createStream(StreamCreationRequestV28DTO createStreamRequest, Long organizationId, String subUnitCode, String accessToken);
  List<StreamListElementDTO> getStreams(Long organizationId, String subUnitCode, String accessToken);
  List<ProgressResponseElementV28DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String subUnitCode, String accessToken);
}
