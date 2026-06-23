package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String> {
  Optional<Campaign> findByExternalId(String externalId);
}
