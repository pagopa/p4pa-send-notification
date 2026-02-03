package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.send.controller.generated.StreamsApi;
import it.gov.pagopa.pu.send.dto.generated.SendStreamDTO;
import it.gov.pagopa.pu.send.service.SendFacadeService;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SendStreamController implements StreamsApi {

  private final SendFacadeService sendFacadeService;

  public SendStreamController(SendFacadeService sendFacadeService) {
    this.sendFacadeService = sendFacadeService;
  }

  @Override
  public ResponseEntity<List<ProgressResponseElementV25DTO>> getStreamEvents(Long organizationId, String streamId, String lastEventId) {
    log.info("retrieve stream events for organizationId {}", organizationId);
    String accessToken = SecurityUtils.getAccessToken();
    return new ResponseEntity<>(
      sendFacadeService.getStreamEvents(streamId, lastEventId ,organizationId, accessToken),
      HttpStatus.OK);
  }

  @Override
  public ResponseEntity<SendStreamDTO> getStreamByOrganizationId(Long organizationId) {
    log.info("Retrieve stream for organization with id {}", organizationId);
    String accessToken = SecurityUtils.getAccessToken();
    return new ResponseEntity<>(
      sendFacadeService.getStreamByOrganizationId(organizationId, accessToken),
      HttpStatus.OK
    );
  }
}
