package it.gov.pagopa.pu.send.connector.pagopa.send.config;

import it.gov.pagopa.pu.send.config.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.pagopa.send")
@SuperBuilder
@NoArgsConstructor
public class PagopaSendApiClientConfig extends ApiClientConfig {
}
