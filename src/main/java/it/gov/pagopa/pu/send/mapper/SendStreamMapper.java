package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO;
import it.gov.pagopa.pu.send.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.send.model.SendStream;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SendStreamMapper {

  public StreamMetadataResponseV25DTO mapToStreamMetadataResponseV25DTO(SendStream sendStream) {
    StreamMetadataResponseV25DTO streamMetadataResponseV25DTO = new StreamMetadataResponseV25DTO();
    streamMetadataResponseV25DTO.setStreamId(UUID.fromString(sendStream.getStreamId()));
    streamMetadataResponseV25DTO.setTitle(sendStream.getTitle());
    streamMetadataResponseV25DTO.setEventType(StreamMetadataResponseV25DTO.EventTypeEnum.valueOf(sendStream.getEventType()));
    return streamMetadataResponseV25DTO;
  }

  public SendStream mapToSendStream(StreamMetadataResponseV25DTO streamMetadataResponseV25DTO, Long organizationId) {
    SendStream sendStream = new SendStream();
    sendStream.setStreamId(streamMetadataResponseV25DTO.getStreamId().toString());
    sendStream.setOrganizationId(organizationId);
    sendStream.setEventType(streamMetadataResponseV25DTO.getEventType().getValue());
    sendStream.setTitle(streamMetadataResponseV25DTO.getTitle());
    return sendStream;
  }

  public SendStreamDTO mapToSendStreamDTO(SendStream sendStream) {
    SendStreamDTO sendStreamDTO = new SendStreamDTO();
    sendStreamDTO.setStreamId(sendStream.getStreamId());
    sendStreamDTO.setOrganizationId(sendStream.getOrganizationId());
    sendStreamDTO.setTitle(sendStream.getTitle());
    sendStreamDTO.setEventType(sendStream.getEventType());
    sendStreamDTO.setLastEventId(sendStream.getLastEventId());
    return sendStreamDTO;
  }

}
