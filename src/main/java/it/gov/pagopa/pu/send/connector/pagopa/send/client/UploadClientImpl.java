package it.gov.pagopa.pu.send.connector.pagopa.send.client;

import it.gov.pagopa.pu.send.dto.DocumentDTO;

import java.net.URI;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UploadClientImpl implements UploadClient{

  private final RestTemplate restTemplate;

  public UploadClientImpl(RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
  }

  @Override
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
