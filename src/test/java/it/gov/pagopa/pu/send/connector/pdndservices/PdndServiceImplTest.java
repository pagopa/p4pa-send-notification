package it.gov.pagopa.pu.send.connector.pdndservices;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pdndservices.dto.generated.PdndAuthData;
import it.gov.pagopa.pu.send.connector.organization.service.OrganizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdndServiceImplTest {

  @Mock
  private PdndCacheService pdndCacheServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private PdndServiceImpl pdndService;

  private static final String ACCESS_TOKEN = "AccessToken";
  private PdndAuthData pdndAuthData;

  @BeforeEach
  void setUp() {
    pdndAuthData = new PdndAuthData();
    pdndAuthData.setAccessToken(ACCESS_TOKEN);

    pdndService = new PdndServiceImpl(pdndCacheServiceMock, organizationServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(pdndCacheServiceMock, organizationServiceMock);
  }

  @Test
  void givenAccessTokenWhenResolvePdndAccessTokenThenIsValid() {
    // Given
    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setPdndEnabled(true);
    OffsetDateTime offsetDateTime = OffsetDateTime.of(2026, 6, 19, 12, 0, 0, 0, ZoneOffset.UTC);
    pdndAuthData.setExpiration(offsetDateTime.plusHours(1));
    try(MockedStatic<OffsetDateTime> offsetDateTimeMock = Mockito.mockStatic(OffsetDateTime.class)) {
      offsetDateTimeMock.when(OffsetDateTime::now).thenReturn(offsetDateTime);
      when(pdndCacheServiceMock.getPdndAccessToken(ACCESS_TOKEN, organizationId, null)).thenReturn(pdndAuthData);
      when(organizationServiceMock.getOrganization(organizationId, ACCESS_TOKEN)).thenReturn(organization);

      // When
      String result = pdndService.resolvePdndAccessToken(organizationId, ACCESS_TOKEN);

      // Then
      assertEquals(ACCESS_TOKEN, result);
    }
  }

  @Test
  void givenAccessTokenWhenResolvePdndAccessTokenThenIsExpired() {
    // Given
    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setPdndEnabled(true);
    OffsetDateTime offsetDateTime = OffsetDateTime.of(2026, 6, 19, 12, 0, 0, 0, ZoneOffset.UTC);
    pdndAuthData.setExpiration(offsetDateTime.minusHours(1));
    try(MockedStatic<OffsetDateTime> offsetDateTimeMock = Mockito.mockStatic(OffsetDateTime.class)) {
      offsetDateTimeMock.when(OffsetDateTime::now).thenReturn(offsetDateTime);
      when(pdndCacheServiceMock.getPdndAccessToken(ACCESS_TOKEN, organizationId, null)).thenReturn(pdndAuthData);
      doNothing().when(pdndCacheServiceMock).evictPdndAccessToken(ACCESS_TOKEN, organizationId, null);
      when(organizationServiceMock.getOrganization(organizationId, ACCESS_TOKEN)).thenReturn(organization);

      // When
      String result = pdndService.resolvePdndAccessToken(organizationId, ACCESS_TOKEN);

      // Then
      assertEquals(ACCESS_TOKEN, result);
      verify(pdndCacheServiceMock, times(2)).getPdndAccessToken(ACCESS_TOKEN, organizationId, null);
    }
  }

  @Test
  void givenPdndEnabledFalseWhenResolvePdndAccessTokenThenNull() {
    // Given
    Long organizationId = 1L;
    Organization organization = new Organization();
    organization.setPdndEnabled(false);
    when(organizationServiceMock.getOrganization(organizationId, ACCESS_TOKEN)).thenReturn(organization);

    // When
    String result = pdndService.resolvePdndAccessToken(organizationId, ACCESS_TOKEN);

    // Then
    assertNull(result);
  }
}
