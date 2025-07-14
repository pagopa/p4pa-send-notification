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
  /**
   * Get Stream Events
   * If streamId in input is null, find last stream for this organization and take streamId
   * @param streamId stream identifier (optional)
   * @param lastEventId (optional)
   * @return List&lt;ProgressResponseElementV25DTO&gt;
   * @throws NotFoundException if doesn't exist at leat one stream
   */
  List<ProgressResponseElementV25DTO> getStreamEvents(String streamId, String lastEventId, Long organizationId, String accessToken);
}
