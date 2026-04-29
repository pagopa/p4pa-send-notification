package it.gov.pagopa.pu.send.connector.pdnd.config;

import it.gov.pagopa.pu.send.config.rest.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.pdnd-services")
@SuperBuilder
@NoArgsConstructor
public class PagopaPdndApiClientConfig extends ApiClientConfig {

}
