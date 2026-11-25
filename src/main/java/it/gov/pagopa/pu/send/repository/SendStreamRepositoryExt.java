package it.gov.pagopa.pu.send.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.pu.send.model.SendStream;

import java.util.List;

public interface SendStreamRepositoryExt {
  List<SendStream> findByOrganizationIdAndOrgSubUnitCode(Long organizationId, String orgSubUnitCode);
  UpdateResult updateLastEventId(String streamId, String lastEventId);
}
