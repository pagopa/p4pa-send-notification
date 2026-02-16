package it.gov.pagopa.pu.common.pii.dto;

import lombok.Getter;

@Getter
public class TestPIIDTO implements PIIDTO {
  private final String fullName;
  private final String email;

  public TestPIIDTO(String fullName, String email) {
    this.fullName = fullName;
    this.email = email;
  }
}
