package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.mapper.CreateNotificationRequest2SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceImplTest {

  @Mock
  private SendNotificationRepository sendNotificationRepositoryMock;

  @Mock
  private CreateNotificationRequest2SendNotificationMapper mapper;

  @InjectMocks
  private SendNotificationServiceImpl sendNotificationService;

  @Test
  void givenCreateNotificationRequestWhenCreateSendNotificationThenReturnCreateNotificationResponse(){
    // Given
    CreateNotificationRequest request = new CreateNotificationRequest();
    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId("SENDNOTIFICATIONID");

    // When
    Mockito.when(mapper.map(request)).thenReturn(sendNotification);
    Mockito.when(sendNotificationRepositoryMock.insert(sendNotification)).thenReturn(sendNotification);

    CreateNotificationResponse response = sendNotificationService.createSendNotification(request);

    // Then
    Mockito.verify(sendNotificationRepositoryMock).insert(sendNotification);
    Assertions.assertNotNull(response);
    Assertions.assertEquals("SENDNOTIFICATIONID", response.getSendNotificationId());
  }
}
