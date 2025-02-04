package it.gov.pagopa.pu.send.config;

import static org.junit.jupiter.api.Assertions.*;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class MongoHealthIndicatorTest {

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private MongoHealthIndicator mongoHealthIndicator;

  @Test
  void testDoHealthCheck() {
    Document mockResult = new Document("maxWireVersion", 6);
    Mockito.when(mongoTemplate.executeCommand("{ isMaster: 1 }")).thenReturn(mockResult);

    Health.Builder builder = new Health.Builder();
    mongoHealthIndicator.doHealthCheck(builder);
    Health health = builder.build();

    assertEquals(Health.up().withDetail("maxWireVersion", 6).build(), health);
  }
}
