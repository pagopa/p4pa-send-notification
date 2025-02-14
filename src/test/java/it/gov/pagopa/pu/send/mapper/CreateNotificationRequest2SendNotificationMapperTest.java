package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.Document;
import it.gov.pagopa.pu.send.dto.generated.Recipient;
import it.gov.pagopa.pu.send.dto.generated.Recipient.RecipientTypeEnum;
import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.model.SendNotification;
import it.gov.pagopa.pu.send.util.TestUtils;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateNotificationRequest2SendNotificationMapperTest {

  private final CreateNotificationRequest2SendNotificationMapper mapper = new CreateNotificationRequest2SendNotificationMapper();

  @Test
  void givenCreateNotificationRequestWhenMapThenOk(){
    // Given
    Recipient recipient = new Recipient();
    recipient.setRecipientType(RecipientTypeEnum.PF);
    recipient.setTaxId("RSSMRA80L05F593A");

    Document document = new Document();
    document.setFileName("test.pdf");
    document.setContentType("application/pdf");
    document.setDigest("sha256");

    CreateNotificationRequest request = new CreateNotificationRequest();
    request.setPaProtocolNumber("Prot_001");
    request.setRecipient(recipient);
    request.setDocuments(Collections.singletonList(document));

    // When
    SendNotification result = mapper.map(request);

    // Then
    TestUtils.checkNotNullFields(result, "sendNotificationId","notificationRequestId");

    Assertions.assertNotNull(result);
    Assertions.assertEquals("PF", result.getSubjectType());
    Assertions.assertEquals("RSSMRA80L05F593A", result.getFiscalCode());
    Assertions.assertEquals("test.pdf", result.getDocuments().getFirst().getFileName());
    Assertions.assertEquals("application/pdf", result.getDocuments().getFirst().getContentType());
    Assertions.assertEquals("sha256", result.getDocuments().getFirst().getDigest());
    Assertions.assertEquals(NotificationStatus.WAITING_FILE, result.getStatus());
    Assertions.assertEquals(FileStatus.WAITING, result.getDocuments().getFirst().getStatus());
  }
}
