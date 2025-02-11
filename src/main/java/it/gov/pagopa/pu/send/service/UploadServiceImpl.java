package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.DocumentDTO;
import it.gov.pagopa.pu.send.exception.InvalidSignatureException;
import it.gov.pagopa.pu.send.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
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
public class UploadServiceImpl implements UploadService{

  private final RestTemplate restTemplate;

  public UploadServiceImpl(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Override
  public Optional<String> uploadFileToS3(String sendNotificationId, DocumentDTO documentDTO)
    throws IOException, NoSuchAlgorithmException {
    //TODO edit file retrieve with P4ADEV-2148
    String filePath = "src/main/resources/tmp/" + sendNotificationId + "_" + documentDTO.getFileName();
    File file = new File(filePath);

    if (!file.exists() || !file.isFile()) {
      throw new FileNotFoundException("File not found: " + documentDTO.getFileName());
    }

    if(!FileUtils.calculateFileHash(file).equals(documentDTO.getDigest()))
      throw new InvalidSignatureException("File "+documentDTO.getFileName()+" has not a valid signature");

    byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(documentDTO.getContentType()));
    headers.add("x-amz-meta-secret", documentDTO.getSecret());
    headers.add("x-amz-checksum-sha256", documentDTO.getDigest());

    HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);
    ResponseEntity<String> response;
    try {
      response = restTemplate.exchange(
        URI.create(documentDTO.getUrl()), HttpMethod.valueOf(documentDTO.getHttpMethod()), entity, String.class);
    } catch (Exception e) {
      log.error("Failed to upload {}: {}", documentDTO.getFileName(), e.getMessage(), e);
      return Optional.empty();
    }

    if(response.getStatusCode().is2xxSuccessful()) {
      return Optional.ofNullable(response.getHeaders().getFirst("x-amz-version-id"));
    } else {
      log.error("Upload failed for {} with status: {}", documentDTO.getFileName(), response.getStatusCode());
      return Optional.empty();
    }
  }
}
