package it.gov.pagopa.pu.send.connector.client;

import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadRequestDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.PreLoadResponseDTO;
import java.util.List;

public interface SendClient {
  List<PreLoadResponseDTO> preloadFiles(List<PreLoadRequestDTO> preLoadRequestDTO);
}
