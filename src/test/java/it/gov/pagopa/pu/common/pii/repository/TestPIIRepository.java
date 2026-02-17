package it.gov.pagopa.pu.common.pii.repository;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.TestFullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.TestPIIDTO;
import it.gov.pagopa.pu.common.pii.mapper.BaseEntityPIIMapper;
import it.gov.pagopa.pu.send.model.TestNoPIIEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public class TestPIIRepository extends BasePIIRepository<TestFullEntityPIIDTO, TestNoPIIEntity, TestPIIDTO, Long> {

  public TestPIIRepository(
    BaseEntityPIIMapper<TestFullEntityPIIDTO, TestNoPIIEntity, TestPIIDTO> piiMapper,
    PersonalDataService personalDataService,
    MongoRepository<TestNoPIIEntity, Long> noPIIRepository) {
    super(piiMapper, personalDataService, noPIIRepository);
  }

  @Override
  protected void setId(TestFullEntityPIIDTO fullDTO, Long id) {
    fullDTO.setId(id);
  }

  @Override
  protected void setId(TestNoPIIEntity noPii, Long id) {
    noPii.setId(id);
  }

  @Override
  protected Long getId(TestNoPIIEntity noPii) {
    return noPii.getId();
  }

  @Override
  protected Class<TestPIIDTO> getPIITDTOClass() {
    return TestPIIDTO.class;
  }

  @Override
  protected PersonalDataType getPIIPersonalDataType() {
    return PersonalDataType.SEND_NOTIFICATION;
  }
}
