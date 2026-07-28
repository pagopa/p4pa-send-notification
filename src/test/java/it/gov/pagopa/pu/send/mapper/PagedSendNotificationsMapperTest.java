package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.PagedSendNotifications;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PagedSendNotificationsMapperTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private final PagedSendNotificationsMapper mapper = new PagedSendNotificationsMapper();

  @Test
  void whenMapToPagedSendNotificationsThenOk() {
    List<SendNotificationNoPII> notifications = podamFactory.manufacturePojo(List.class, SendNotificationNoPII.class);
    Pageable pageable = PageRequest.of(0, 2);
    Page<SendNotificationNoPII> notificationPage = new PageImpl<>(notifications, pageable, 5L);

    PagedSendNotifications result = mapper.mapToPagedSendNotifications(notificationPage);

    assertNotNull(result);
    TestUtils.checkNotNullFields(result);
    assertEquals(notificationPage.getContent(), result.getContent());
    assertEquals(notificationPage.getSize(), result.getSize());
    assertEquals(notificationPage.getTotalPages(), result.getTotalPages());
    assertEquals(notificationPage.getTotalElements(), result.getTotalElements());
    assertEquals(notificationPage.getNumber(), result.getNumber());
  }
}
