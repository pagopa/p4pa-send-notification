package it.gov.pagopa.pu.send.connector.pagopa.workflow.client;

import it.gov.pagopa.pu.send.connector.pagopa.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.controller.generated.SendNotificationApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorflowApiClientTest {
  @Mock
  private WorkflowApisHolder workflowApisHolderMock;
  @Mock
  private SendNotificationApi sendNotificationApiMock;

  private WorkflowApiClient workflowApiClient;

  @BeforeEach
  void setUp() {
    workflowApiClient = new WorkflowApiClient(workflowApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApisHolderMock
    );
  }

  @Test
  void whenSendNotificationProcessThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "sendNotificationId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getSendNotificationApi(accessToken))
      .thenReturn(sendNotificationApiMock);
    Mockito.when(sendNotificationApiMock.sendNotificationProcess(Mockito.same(sendNotificationId)))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.sendNotificationProcess(sendNotificationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

}
