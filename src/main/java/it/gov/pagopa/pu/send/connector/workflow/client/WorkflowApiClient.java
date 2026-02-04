package it.gov.pagopa.pu.send.connector.workflow.client;

import it.gov.pagopa.pu.send.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId, String accessToken) {
    return workflowApisHolder.getSendNotificationApi(accessToken)
      .sendNotificationProcess(sendNotificationId);
  }

  public WorkflowCreatedDTO sendNotificationStreamConsume(String sendStreamId, Long organizationId, String accessToken) {
    return workflowApisHolder.getSendNotificationApi(accessToken)
      .consumeSendStream(sendStreamId, organizationId);
  }
}
