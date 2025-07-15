package it.gov.pagopa.pu.send.connector.pagopa.send;

import it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamCreationRequestV25DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamListElementDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO;
import it.gov.pagopa.pu.send.exception.NotFoundException;
import java.util.List;

public interface SendStreamService {
  StreamMetadataResponseV25DTO createStream(StreamCreationRequestV25DTO createStreamRequest, Long organizationId, String accessToken);
  List<StreamListElementDTO> getStreams(Long organizationId, String accessToken);
  List<ProgressResponseElementV25DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String accessToken);
}
