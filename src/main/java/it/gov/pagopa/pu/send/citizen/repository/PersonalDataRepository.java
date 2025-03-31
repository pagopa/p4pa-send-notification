package it.gov.pagopa.pu.send.citizen.repository;

import it.gov.pagopa.pu.send.citizen.model.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {

}
