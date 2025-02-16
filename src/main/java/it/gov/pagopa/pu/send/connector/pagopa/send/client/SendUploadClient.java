package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.config.RestTemplateConfig;
import it.gov.pagopa.pu.send.connector.pagopa.send.config.PagopaSendApiClientConfig;
import it.gov.pagopa.pu.send.dto.DocumentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

@Service
@Slf4j
public class SendUploadClient {

  private final RestTemplate restTemplate;

  public SendUploadClient(
    RestTemplateBuilder restTemplateBuilder,
    PagopaSendApiClientConfig clientConfig
  ){
    this.restTemplate = restTemplateBuilder.build();
    if(clientConfig.isPrintBodyWhenError()){
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("SEND-UPLOAD"));
    }
  }

  public Optional<String> upload(DocumentDTO doc, byte[] fileBytes) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(doc.getContentType()));
    headers.add("x-amz-meta-secret", doc.getSecret());
    headers.add("x-amz-checksum-sha256", doc.getDigest());

    HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);

    ResponseEntity<String> response = restTemplate.exchange(
      URI.create(doc.getUrl()), HttpMethod.valueOf(doc.getHttpMethod()), entity, String.class);

    if(response.getStatusCode().is2xxSuccessful()) {
      return Optional.ofNullable(response.getHeaders().getFirst("x-amz-version-id"));
    } else {
      log.error("Upload failed for {} with status: {}", doc.getFileName(), response.getStatusCode());
      return Optional.empty();
    }
  }
}
