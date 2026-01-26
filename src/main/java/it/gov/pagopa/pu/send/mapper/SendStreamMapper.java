package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.util.Constants;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Service
public class SendStreamMapper {
  public StreamMetadataResponseV25DTO mapToStreamMetadataResponseV25DTO(SendStream sendStream) {  //TODO P4ADEV-3717 check missing mapping of available fields: String organizationIpaCode, String lastEventId (this is !important! for reading stream)
    StreamMetadataResponseV25DTO streamMetadataResponseV25DTO = new StreamMetadataResponseV25DTO();
    streamMetadataResponseV25DTO.setStreamId(UUID.fromString(sendStream.getStreamId()));
    streamMetadataResponseV25DTO.setTitle(sendStream.getTitle());
    streamMetadataResponseV25DTO.setEventType(StreamMetadataResponseV25DTO.EventTypeEnum.valueOf(sendStream.getEventType()));
    streamMetadataResponseV25DTO.setActivationDate(LocalDate.now().atStartOfDay(Constants.ZONEID).toOffsetDateTime()); //TODO P4ADEV-3717 check fixed mapping
    streamMetadataResponseV25DTO.setGroups(Collections.emptyList()); //TODO P4ADEV-3717 check fixed mapping
    streamMetadataResponseV25DTO.setFilterValues(Collections.emptyList()); //TODO P4ADEV-3717 check fixed mapping
    streamMetadataResponseV25DTO.setDisabledDate(null); //TODO P4ADEV-3717 check missing mapping
    streamMetadataResponseV25DTO.setVersion(null); //TODO P4ADEV-3717 check missing mapping
    return streamMetadataResponseV25DTO;
  }

  public SendStream mapToSendStream(StreamMetadataResponseV25DTO streamMetadataResponseV25DTO) {  //TODO P4ADEV-3717 check missing mapping of available fields: OffsetDateTime activationDate, OffsetDateTime disabledDate, List<String> groups, List<String> filterValues, String version
    SendStream sendStream = new SendStream();
    sendStream.setStreamId(streamMetadataResponseV25DTO.getStreamId().toString());
    sendStream.setEventType(streamMetadataResponseV25DTO.getEventType().getValue());
    sendStream.setTitle(streamMetadataResponseV25DTO.getTitle());
    sendStream.setOrganizationIpaCode(null); //TODO P4ADEV-3717 check missing mapping
    sendStream.setLastEventId(null); //TODO P4ADEV-3717 check missing mapping
    return sendStream;
  }
}
