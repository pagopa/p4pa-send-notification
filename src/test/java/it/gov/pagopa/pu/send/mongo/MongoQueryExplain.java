package it.gov.pagopa.pu.send.mongo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * It requires a file on src/test/resources/secrets folder, mongodbConnectionString.properties, having defined the mongo config
 * (At least the connection string: spring.data.mongodb.uri)
 * Comment the @Disabled annotation to use it
 */
@Disabled
@SpringBootTest
@TestPropertySource(
  locations = {
    "classpath:/secrets/mongodbConnectionString.properties"
  },
  properties = {
    "spring.datasource.citizen.driver-class-name=org.h2.Driver",
    "spring.datasource.citizen.jdbc-url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
    "spring.datasource.citizen.username=sa",
    "spring.datasource.citizen.password=sa",
  })
@Import({
  MongoTestUtilitiesService.class,
  MongoTestUtilitiesService.TestMongoConfiguration.class,
  SimpleMeterRegistry.class
})
@Slf4j
@SuppressWarnings({"squid:S3577", "NewClassNamingConvention"}) // suppressing class name not match alert: we are not using the Test suffix in order to let not execute this test by default configuration.
class MongoQueryExplain {

  static {
    ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
  }

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private SendNotificationNoPIIRepository repository;

  @Test
  void explainQuery() {
    MongoTestUtilitiesService.startMongoCommandListener();

    repository.findByIdAndOrganizationId("NOTIFICATIONID", 1L);

    MongoTestUtilitiesService.stopAndPrintMongoCommands();
    org.bson.Document explainResult = mongoTemplate.executeCommand(new org.bson.Document("getLastRequestStatistics", 1));
    log.info("Last query explain:\n{}", explainResult.toJson());
  }
}
