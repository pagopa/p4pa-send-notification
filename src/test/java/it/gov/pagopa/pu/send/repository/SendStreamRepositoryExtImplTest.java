package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.model.SendStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SendStreamRepositoryExtImplTest {

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private SendStreamRepositoryExtImpl repository;

  @Test
  void givenIdAndOrganizationIdThenVerify() {
    //GIVEN
    String orgIpaCode = "ipaCode";

    List<SendStream> expectedResponse = new ArrayList<>();
    expectedResponse.add(SendStream.builder().build());

    Mockito.when(mongoTemplate.find(Mockito.any(Query.class), Mockito.eq(
      SendStream.class))).thenReturn(expectedResponse);

    //WHEN
    List<SendStream> actualResult = repository.findByIpaCode(orgIpaCode);

    //THEN
    Assertions.assertEquals(expectedResponse, actualResult);
    Mockito.verify(mongoTemplate, Mockito.times(1))
      .find(Mockito.any(Query.class), Mockito.eq(SendStream.class));
  }

  @Test
  void givenStreamIdAndLastEventIdThenVerify() {
    //GIVEN
    String streamId = "streamId";
    String lastEventId = "lastEventId";

    UpdateResult expectedResponse = Mockito.mock(UpdateResult.class);

    Mockito.when(
      mongoTemplate.updateFirst(
        Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(SendStream.class)
      )
    ).thenReturn(expectedResponse);

    Mockito.when(expectedResponse.getModifiedCount()).thenReturn(1L);

    //WHEN
    UpdateResult actualResult = repository.updateLastEventId(streamId, lastEventId);

    //THEN
    Assertions.assertEquals(expectedResponse, actualResult);
    Assertions.assertEquals(1L, actualResult.getModifiedCount());
    Mockito.verify(mongoTemplate, Mockito.times(1))
      .updateFirst(
        Mockito.any(Query.class),
        Mockito.any(Update.class),
        Mockito.eq(SendStream.class)
      );
  }

}
