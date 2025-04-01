package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.send.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.send.dto.TestFullPIIDTO;
import it.gov.pagopa.pu.send.dto.TestPIIDTO;
import it.gov.pagopa.pu.send.mapper.BasePIIMapper;
import it.gov.pagopa.pu.send.model.TestNoPIIEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public class TestPIIRepository extends BasePIIRepository<TestFullPIIDTO, TestNoPIIEntity, TestPIIDTO, Long> {

  public TestPIIRepository(
    BasePIIMapper<TestFullPIIDTO, TestNoPIIEntity, TestPIIDTO> piiMapper,
    PersonalDataService personalDataService,
    MongoRepository<TestNoPIIEntity, Long> noPIIRepository) {
    super(piiMapper, personalDataService, noPIIRepository);
  }

  @Override
  void setId(TestFullPIIDTO fullDTO, Long id) {
    fullDTO.setId(id);
  }

  @Override
  void setId(TestNoPIIEntity noPii, Long id) {
    noPii.setId(id);
  }

  @Override
  Long getId(TestNoPIIEntity noPii) {
    return noPii.getId();
  }

  @Override
  Class<TestPIIDTO> getPIITDTOClass() {
    return TestPIIDTO.class;
  }

  @Override
  PersonalDataType getPIIPersonalDataType() {
    return PersonalDataType.SEND_NOTIFICATION;
  }
}
