package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.mapper.SendNotificationMapper;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.repository.SendNotificationRepository;
import java.util.List;
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
  private SendNotificationMapper sendNotificationMapperMock;
  @Mock
  private SequenceGeneratorService sequenceGeneratorServiceMock;

  @InjectMocks
  private SendNotificationServiceImpl sendNotificationService;

  @Test
  void givenCreateNotificationRequestWhenCreateSendNotificationThenReturnCreateNotificationResponse(){
    // Given
    CreateNotificationRequest request = CreateNotificationRequest
      .builder()
      .preloadId("TEST.pdf")
      .contentType("application/pdf")
      .sha256("ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ")
      .build();

    SendNotification sendNotification = SendNotification.builder()
      .preloadId("TEST.pdf")
      .contentType("application/pdf")
      .expectedFileDigest("ZjVlZjRiYjE4YTc4YTkwZTFiOGYyMTg4ZTBjYTdmOGU2MDRkZGEzMjllODRhNGQzNmE4OWNjYWY1MDA5MTBmNQ")
      .build();

    Long generatedId = 1L;

    // When
    Mockito.when(sequenceGeneratorServiceMock.generateSequence(SendNotification.SEQUENCE_NAME)).thenReturn(generatedId);
    Mockito.when(sendNotificationMapperMock.apply(request)).thenReturn(sendNotification);

    CreateNotificationResponse response = sendNotificationService.createSendNotification(List.of(request));

    // Then
    Mockito.verify(sendNotificationRepositoryMock).createIfNotExists(generatedId, sendNotification);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(generatedId, response.getSendNotificationId());
    Assertions.assertEquals(NotificationStatus.WAITING_FILE.name(), response.getStatus());
  }
}
