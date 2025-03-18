package it.gov.pagopa.pu.send.connector.pagopa.workflow.service;

import it.gov.pagopa.pu.send.connector.pagopa.workflow.client.WorkflowApiClient;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl implements WorkflowService {
  private final WorkflowApiClient workflowApiClient;

  public WorkflowServiceImpl(WorkflowApiClient workflowApiClient) {
    this.workflowApiClient = workflowApiClient;
  }

  @Override
  public WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId, String accessToken) {
    return workflowApiClient.sendNotificationProcess(sendNotificationId, accessToken);
  }
}
