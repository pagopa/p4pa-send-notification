package it.gov.pagopa.pu.send.connector.workflow.service;

import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface WorkflowService {

  WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId, String accessToken);
  WorkflowCreatedDTO sendNotificationStreamConsume(String sendStreamId, String accessToken);
}
