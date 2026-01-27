package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.model.SendStream;

import java.util.List;

public interface SendStreamRepositoryExt {
  List<SendStream> findByIpaCode(String ipaCode);
}
