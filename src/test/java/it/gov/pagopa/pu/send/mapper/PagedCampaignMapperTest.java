package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.PagedCampaign;
import it.gov.pagopa.pu.send.model.Campaign;
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

class PagedCampaignMapperTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private final PagedCampaignMapper mapper = new PagedCampaignMapper();

  @Test
  void whenMapToPagedCampaignThenOk() {
    List<Campaign> campaigns = podamFactory.manufacturePojo(List.class, Campaign.class);
    Pageable pageable = PageRequest.of(0, 2);
    Page<Campaign> campaignPage = new PageImpl<>(campaigns, pageable, 5L);

    PagedCampaign result = mapper.mapToPagedCampaign(campaignPage);

    assertNotNull(result);
    TestUtils.checkNotNullFields(result);
    assertEquals(campaignPage.getContent(), result.getContent());
    assertEquals(campaignPage.getSize(), result.getSize());
    assertEquals(campaignPage.getTotalPages(), result.getTotalPages());
    assertEquals(campaignPage.getTotalElements(), result.getTotalElements());
    assertEquals(campaignPage.getNumber(), result.getNumber());
  }
}
