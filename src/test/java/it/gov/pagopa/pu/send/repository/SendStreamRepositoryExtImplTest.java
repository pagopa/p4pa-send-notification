package it.gov.pagopa.pu.send.repository;

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

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SendStreamRepositoryExtImplTest {

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private SendStreamRepositoryExtImpl repository;

  @Test
  void givenIdAndOrganizationIdThenVerify() {
    String orgIpaCode = "ipaCode";

    List<SendStream> expectedResponse = new ArrayList<>();
    expectedResponse.add(SendStream.builder().build());

    Mockito.when(mongoTemplate.find(Mockito.any(Query.class), Mockito.eq(
      SendStream.class))).thenReturn(expectedResponse);

    List<SendStream> actualResult = repository.findByIpaCode(orgIpaCode);

    Assertions.assertEquals(expectedResponse, actualResult);
    Mockito.verify(mongoTemplate, Mockito.times(1))
      .find(Mockito.any(Query.class), Mockito.eq(SendStream.class));
  }

}
