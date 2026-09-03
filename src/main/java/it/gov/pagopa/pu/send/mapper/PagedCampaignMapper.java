package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.model.SendCampaign;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PagedCampaignMapper {

  public PagedCampaign mapToPagedCampaign(Page<SendCampaign> campaignPage) {
    return PagedCampaign.builder()
      .content(campaignPage.getContent())
      .size((long) campaignPage.getSize())
      .totalPages((long) campaignPage.getTotalPages())
      .totalElements(campaignPage.getTotalElements())
      .number(campaignPage.getNumber())
      .build();
  }
}
