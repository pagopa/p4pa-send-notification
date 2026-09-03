package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendCampaign;
import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends MongoRepository<SendCampaign, String>, CampaignRepositoryExt {
  Optional<SendCampaign> findByExternalIdAndOrganizationIdAndOrgSubUnitCode(String externalId, Long organizationId, String orgSubUnitCode);
  List<CampaignIdView> findAllCampaignIdsByOrderByCampaignIdAsc();
}
