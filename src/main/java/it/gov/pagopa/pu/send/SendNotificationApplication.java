package it.gov.pagopa.pu.send;

import it.gov.pagopa.pu.send.util.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(
  exclude = {ErrorMvcAutoConfiguration.class},
  scanBasePackages = "it.gov.pagopa.pu"
)
public class SendNotificationApplication {

	public static void main(String[] args) {
    TimeZone.setDefault(Constants.DEFAULT_TIMEZONE);
		SpringApplication.run(SendNotificationApplication.class, args);
	}

}
