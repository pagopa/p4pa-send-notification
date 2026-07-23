package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.PagedSendNotifications;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PagedSendNotificationsMapper {

  public PagedSendNotifications mapToPagedSendNotifications(Page<SendNotificationNoPII> sendNotificationsPage) {
    return PagedSendNotifications.builder()
      .content(sendNotificationsPage.getContent())
      .size((long) sendNotificationsPage.getSize())
      .totalPages((long) sendNotificationsPage.getTotalPages())
      .totalElements(sendNotificationsPage.getTotalElements())
      .number(sendNotificationsPage.getNumber())
      .build();
  }
}
