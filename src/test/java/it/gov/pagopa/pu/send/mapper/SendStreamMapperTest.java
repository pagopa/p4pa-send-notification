package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.connector.send.generated.dto.StreamMetadataResponseV25DTO;
import it.gov.pagopa.pu.send.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.send.model.SendStream;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SendStreamMapperTest {

  public static final UUID STREAM_ID = UUID.randomUUID();
  public static final Long ORGANIZATION_ID = 1L;
  public static final StreamMetadataResponseV25DTO.EventTypeEnum EVENT_TYPE_ENUM = StreamMetadataResponseV25DTO.EventTypeEnum.STATUS;
  public static final String TITLE = "title";
  public static final String LAST_EVENT_ID = "lastEventId";
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
    //When
    StreamMetadataResponseV25DTO actualResponse =
      sendStreamMapper.mapToStreamMetadataResponseV25DTO(sendStream);
    //Then
    Assertions.assertEquals(expectedResponse, actualResponse);
    TestUtils.checkNotNullFields(actualResponse, "disabledDate", "activationDate", "version");
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
        .organizationId(ORGANIZATION_ID)
        .build();
    //When
    SendStream actualResponse =
      sendStreamMapper.mapToSendStream(streamMetadataResponse, ORGANIZATION_ID);
    //Then
    Assertions.assertEquals(expectedResponse, actualResponse);
    TestUtils.checkNotNullFields(actualResponse, "lastEventId");
  }

  @Test
  void testMapToSendStreamDTO() {
    //Given
    SendStream sendStream =
      SendStream.builder()
        .streamId(STREAM_ID.toString())
        .title(TITLE)
        .organizationId(ORGANIZATION_ID)
        .eventType(EVENT_TYPE_ENUM.getValue())
        .lastEventId(LAST_EVENT_ID)
        .build();
    SendStreamDTO expectedResponse =
      SendStreamDTO.builder()
        .streamId(STREAM_ID.toString())
        .title(TITLE)
        .organizationId(ORGANIZATION_ID)
        .eventType(EVENT_TYPE_ENUM.getValue())
        .lastEventId(LAST_EVENT_ID)
        .build();

    //When
    SendStreamDTO actualResult = sendStreamMapper.mapToSendStreamDTO(sendStream);

    //Then
    Assertions.assertEquals(expectedResponse, actualResult);
    TestUtils.checkNotNullFields(actualResult);
  }
}
