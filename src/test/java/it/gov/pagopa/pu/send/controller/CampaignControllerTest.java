package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.service.CampaignService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CampaignControllerTest {
  @Mock
  private CampaignService campaignServiceMock;

  @InjectMocks
  private  CampaignController campaignController;

  @AfterEach
  void verifyNoMoreInteraction(){
    Mockito.verifyNoMoreInteractions(
      campaignServiceMock
    );
  }

  @Test
  void whenFetchAllCampaignIdsThenOk() {
    List<String> expectedIds = List.of("id1", "id2", "id3");

    Mockito.when(campaignServiceMock.fetchAllIds()).thenReturn(expectedIds);

    ResponseEntity<List<String>> response = campaignController.fetchAllCampaignIds();

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(expectedIds, response.getBody());
  }
}
