package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.mapper.SendNotificationPIIMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationPIIRepositoryImpl extends BasePIIRepository<SendNotification, SendNotificationNoPII, SendNotificationPIIDTO, String> implements SendNotificationPIIRepository {

  public SendNotificationPIIRepositoryImpl(
    SendNotificationPIIMapper sendNotificationPIIMapper,
    SendNotificationNoPIIRepository sendNotificationNoPIIRepository,
    PersonalDataService personalDataService) {
    super(sendNotificationPIIMapper, personalDataService, sendNotificationNoPIIRepository);
  }

  @Override
  void setId(SendNotification fullDTO, String id) { fullDTO.setSendNotificationId(id); }

  @Override
  void setId(SendNotificationNoPII noPii, String id) { noPii.setSendNotificationId(id); }

  @Override
  String getId(SendNotificationNoPII noPii) {
    return noPii.getSendNotificationId();
  }

  @Override
  Class<SendNotificationPIIDTO> getPIITDTOClass() {
    return SendNotificationPIIDTO.class;
  }
}
