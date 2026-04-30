package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV28DTO;
import it.gov.pagopa.pu.send.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.send.model.SendStream;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SendStreamMapper {

  public StreamMetadataResponseV28DTO mapToStreamMetadataResponseV28DTO(SendStream sendStream) {
    StreamMetadataResponseV28DTO streamMetadataResponseV28DTO = new StreamMetadataResponseV28DTO();
    streamMetadataResponseV28DTO.setStreamId(UUID.fromString(sendStream.getStreamId()));
    streamMetadataResponseV28DTO.setTitle(sendStream.getTitle());
    streamMetadataResponseV28DTO.setEventType(StreamMetadataResponseV28DTO.EventTypeEnum.valueOf(sendStream.getEventType()));
    return streamMetadataResponseV28DTO;
  }

  public SendStream mapToSendStream(StreamMetadataResponseV28DTO streamMetadataResponseV28DTO, Long organizationId) {
    SendStream sendStream = new SendStream();
    sendStream.setStreamId(streamMetadataResponseV28DTO.getStreamId().toString());
    sendStream.setOrganizationId(organizationId);
    sendStream.setEventType(streamMetadataResponseV28DTO.getEventType().getValue());
    sendStream.setTitle(streamMetadataResponseV28DTO.getTitle());
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
