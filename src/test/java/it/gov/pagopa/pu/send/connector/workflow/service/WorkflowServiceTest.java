package it.gov.pagopa.pu.send.connector.workflow.service;

import it.gov.pagopa.pu.send.connector.workflow.client.WorkflowApiClient;
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
class WorkflowServiceTest {

  @Mock
  private WorkflowApiClient workflowApiClientMock;

  private WorkflowService workflowService;

  @BeforeEach
  void init() {
    workflowService = new WorkflowServiceImpl(
      workflowApiClientMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApiClientMock
    );
  }

  @Test
  void whenSendNotificationProcessThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "sendNotificationId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();

    Mockito.when(workflowApiClientMock.sendNotificationProcess(Mockito.same(sendNotificationId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.sendNotificationProcess(sendNotificationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenSendNotificationStreamConsumeThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    String sendStreamId = "sendStreamId";
    Long organizationId = 1L;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();

    Mockito.when(workflowApiClientMock.sendNotificationStreamConsume(Mockito.same(sendStreamId), Mockito.same(organizationId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.sendNotificationStreamConsume(sendStreamId, organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

}
