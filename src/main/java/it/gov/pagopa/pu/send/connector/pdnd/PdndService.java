package it.gov.pagopa.pu.send.connector.pdnd;

public interface PdndService {
  String resolvePdndAccessToken(Long organizationId, String accessToken);
}
