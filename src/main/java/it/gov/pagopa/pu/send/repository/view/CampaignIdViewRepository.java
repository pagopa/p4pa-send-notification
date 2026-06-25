package it.gov.pagopa.pu.send.repository.view;

import it.gov.pagopa.pu.send.model.view.CampaignIdView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignIdViewRepository extends MongoRepository<CampaignIdView, String> {

}
