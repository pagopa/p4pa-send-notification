package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepositoryExtImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendNotificationEntityExtendedControllerTest {

  @Mock
  private SendNotificationNoPIIRepositoryExtImpl repositoryMock;

  private SendNotificationEntityExtendedController controller;

  @BeforeEach
  void init() {
    controller = new SendNotificationEntityExtendedController(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(repositoryMock);
  }

  @Test
  void updateNotificationStatus() {
    // Given
    Long sendNotificationId = 1L;
    String status = "ERROR";

    // When
    controller.updateNotificationStatus(sendNotificationId, status);

    // Then
    verify(repositoryMock).updateNotificationStatus(String.valueOf(sendNotificationId), NotificationStatus.valueOf(status));
  }
}
