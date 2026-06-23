package it.gov.pagopa.pu.send.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.OrgSubUnit;
import it.gov.pagopa.pu.send.connector.organization.client.OrgSubUnitApiClient;
import it.gov.pagopa.pu.send.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgSubUnitServiceImplTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private OrgSubUnitApiClient orgSubUnitApiClientMock;

  private OrgSubUnitService service;

  @BeforeEach
  void setUp() {
    service = new OrgSubUnitServiceImpl(orgSubUnitApiClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(orgSubUnitApiClientMock);
  }

  @Test
  void whenGetOrgSubUnitByIdThenInvokeClient() {
    String orgSubUnitId = "orgSubUnitId";
    String accessToken = "accessToken";
    OrgSubUnit expectedResult = podamFactory.manufacturePojo(OrgSubUnit.class);

    when(orgSubUnitApiClientMock.getOrgSubUnitById(orgSubUnitId, accessToken))
      .thenReturn(expectedResult);

    OrgSubUnit result = service.getOrgSubUnitById(orgSubUnitId, accessToken);

    assertSame(expectedResult, result);
  }
}
