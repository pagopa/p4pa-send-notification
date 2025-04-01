package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.TestPIIDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestNoPIIEntity implements NoPIIEntity<TestPIIDTO> {
  private Long id;
  private Long personalDataId;

  public TestNoPIIEntity(Long id) {
    this.id = id;
  }

  @Override
  public void setPersonalDataId(Long personalDataId) {
    this.personalDataId = personalDataId;
  }

  @Override
  public Long getPersonalDataId() {
    return personalDataId;
  }
}
