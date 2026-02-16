package it.gov.pagopa.pu.common.pii.dto;

import it.gov.pagopa.pu.send.model.TestNoPIIEntity;
import lombok.Getter;
import lombok.Setter;

// Test implementation classes
@Getter
@Setter
public class TestFullEntityPIIDTO implements FullEntityPIIDTO<TestNoPIIEntity, TestPIIDTO> {
  private String fullName;
  private String email;
  private Long id;
  private TestNoPIIEntity noPII;

  public TestFullEntityPIIDTO(String fullName, String email, Long id) {
    this.fullName = fullName;
    this.email = email;
    this.id = id;
  }

  @Override
  public TestNoPIIEntity getNoPII() {
    return noPII;
  }

  @Override
  public void setNoPII(TestNoPIIEntity noPII) {
    this.noPII = noPII;
  }
}
