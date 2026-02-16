package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.repository.BasePIIRepository;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.pii.SendNotificationPIIDTO;
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
  protected void setId(SendNotification fullDTO, String id) { fullDTO.setSendNotificationId(id); }

  @Override
  protected void setId(SendNotificationNoPII noPii, String id) { noPii.setSendNotificationId(id); }

  @Override
  protected String getId(SendNotificationNoPII noPii) {
    return noPii.getSendNotificationId();
  }

  @Override
  protected Class<SendNotificationPIIDTO> getPIITDTOClass() {
    return SendNotificationPIIDTO.class;
  }

  @Override
  protected PersonalDataType getPIIPersonalDataType() {
    return PersonalDataType.SEND_NOTIFICATION;
  }
}
