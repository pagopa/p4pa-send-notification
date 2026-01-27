package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO;
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

  public SendStream mapToSendStream(StreamMetadataResponseV25DTO streamMetadataResponseV25DTO, String orgIpaCode) {
    SendStream sendStream = new SendStream();
    sendStream.setStreamId(streamMetadataResponseV25DTO.getStreamId().toString());
    sendStream.setOrganizationIpaCode(orgIpaCode);
    sendStream.setEventType(streamMetadataResponseV25DTO.getEventType().getValue());
    sendStream.setTitle(streamMetadataResponseV25DTO.getTitle());
    return sendStream;
  }

}
