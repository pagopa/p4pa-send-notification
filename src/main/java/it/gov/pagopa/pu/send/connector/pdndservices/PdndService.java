package it.gov.pagopa.pu.send.connector.pdndservices;

public interface PdndService {
  String resolvePdndAccessToken(Long organizationId, String accessToken);
}
