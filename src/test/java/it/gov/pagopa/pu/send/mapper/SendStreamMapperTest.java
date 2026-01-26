package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.util.Constants;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

class SendStreamMapperTest {

  public static final UUID STREAM_ID = UUID.randomUUID();
  public static final StreamMetadataResponseV25DTO.EventTypeEnum EVENT_TYPE_ENUM = StreamMetadataResponseV25DTO.EventTypeEnum.STATUS;
  public static final String TITLE = "title";
  private final SendStreamMapper sendStreamMapper = new SendStreamMapper();

  @Test
  void testMapToStreamMetadataResponseV25DTO() {
    //Given
    SendStream sendStream =
      SendStream.builder()
        .streamId(STREAM_ID.toString())
        .title(TITLE)
        .eventType(EVENT_TYPE_ENUM.getValue())
        .build();
    StreamMetadataResponseV25DTO expectedResponse =
       new StreamMetadataResponseV25DTO();
    expectedResponse.setStreamId(STREAM_ID);
    expectedResponse.setTitle(TITLE);
    expectedResponse.setEventType(EVENT_TYPE_ENUM);
    expectedResponse.setActivationDate(LocalDate.now().atStartOfDay(Constants.ZONEID).toOffsetDateTime());
    //When
    StreamMetadataResponseV25DTO actualResponse =
      sendStreamMapper.mapToStreamMetadataResponseV25DTO(sendStream);
    //Then
    Assertions.assertEquals(expectedResponse, actualResponse);
    TestUtils.checkNotNullFields(actualResponse, "disabledDate", "version");
  }

  @Test
  void testMapToSendStream() {
    //Given
    StreamMetadataResponseV25DTO streamMetadataResponse =
      new StreamMetadataResponseV25DTO();
    streamMetadataResponse.setStreamId(STREAM_ID);
    streamMetadataResponse.setTitle(TITLE);
    streamMetadataResponse.setEventType(EVENT_TYPE_ENUM);
    SendStream expectedResponse =
      SendStream.builder()
        .streamId(STREAM_ID.toString())
        .title(TITLE)
        .eventType(EVENT_TYPE_ENUM.getValue())
        .build();
    //When
    SendStream actualResponse =
      sendStreamMapper.mapToSendStream(streamMetadataResponse);
    //Then
    Assertions.assertEquals(expectedResponse, actualResponse);
    TestUtils.checkNotNullFields(actualResponse, "organizationIpaCode", "lastEventId");
  }
}
