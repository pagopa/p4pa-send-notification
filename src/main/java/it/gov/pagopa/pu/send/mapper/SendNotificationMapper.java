package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationMapper {

  private final PersonalDataService personalDataService;

  public SendNotificationMapper(PersonalDataService personalDataService) {
    this.personalDataService = personalDataService;
  }

  public SendNotification map(SendNotificationNoPII noPii) {
    SendNotificationPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), SendNotificationPIIDTO.class);

    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId(noPii.getSendNotificationId());
    sendNotification.setOrganizationId(noPii.getOrganizationId());
    sendNotification.setPaProtocolNumber(noPii.getPaProtocolNumber());
    sendNotification.setSubjectType(noPii.getSubjectType());
    sendNotification.setFiscalCode(pii.getFiscalCode());
    sendNotification.setAddress(pii.getAddress());
    sendNotification.setDenomination(noPii.getDenomination());
    sendNotification.setPayments(noPii.getPayments());
    sendNotification.setDocuments(noPii.getDocuments());
    sendNotification.setStatus(noPii.getStatus());
    sendNotification.setNotificationRequestId(noPii.getNotificationRequestId());
    sendNotification.setIun(noPii.getIun());
    sendNotification.setNotificationFeePolicy(noPii.getNotificationFeePolicy());
    sendNotification.setPhysicalCommunicationType(noPii.getPhysicalCommunicationType());
    sendNotification.setSenderDenomination(noPii.getSenderDenomination());
    sendNotification.setSenderTaxId(noPii.getSenderTaxId());
    sendNotification.setAmount(noPii.getAmount());
    sendNotification.setPaymentExpirationDate(noPii.getPaymentExpirationDate());
    sendNotification.setTaxonomyCode(noPii.getTaxonomyCode());
    sendNotification.setPaFee(noPii.getPaFee());
    sendNotification.setVat(noPii.getVat());
    sendNotification.setPagoPaIntMode(noPii.getPagoPaIntMode());
    sendNotification.setNotificationData(noPii.getNotificationData());
    sendNotification.setNoPII(noPii);

    return sendNotification;
  }
}
