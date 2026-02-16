package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.mapper.BasePIIMapper;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationPIIMapper extends BasePIIMapper<SendNotification, SendNotificationNoPII, SendNotificationPIIDTO> {

  private final PersonalDataService personalDataService;
  private final DataCipherService dataCipherService;

  public SendNotificationPIIMapper(PersonalDataService personalDataService,
    DataCipherService dataCipherService) {
    this.personalDataService = personalDataService;
    this.dataCipherService = dataCipherService;
  }

  @Override
  protected SendNotificationNoPII extractNoPiiEntity(SendNotification fullDTO) {
    SendNotificationNoPII noPII = new SendNotificationNoPII();

    noPII.setSendNotificationId(fullDTO.getSendNotificationId());
    noPII.setOrganizationId(fullDTO.getOrganizationId());
    noPII.setPaProtocolNumber(fullDTO.getPaProtocolNumber());
    noPII.setDocuments(fullDTO.getDocuments());
    noPII.setStatus(fullDTO.getStatus());
    noPII.setNotificationRequestId(fullDTO.getNotificationRequestId());
    noPII.setIun(fullDTO.getIun());
    noPII.setNotificationFeePolicy(fullDTO.getNotificationFeePolicy());
    noPII.setPhysicalCommunicationType(fullDTO.getPhysicalCommunicationType());
    noPII.setSenderDenomination(fullDTO.getSenderDenomination());
    noPII.setSenderTaxId(fullDTO.getSenderTaxId());
    noPII.setAmount(fullDTO.getAmount());
    noPII.setPaymentExpirationDate(fullDTO.getPaymentExpirationDate());
    noPII.setTaxonomyCode(fullDTO.getTaxonomyCode());
    noPII.setPaFee(fullDTO.getPaFee());
    noPII.setVat(fullDTO.getVat());
    noPII.setPagoPaIntMode(fullDTO.getPagoPaIntMode());
    noPII.setLegalFacts(fullDTO.getLegalFacts());
    List<PuRecipientNoPIIDTO> recipients = fullDTO.getPuRecipients().stream().map(
      puRecipient -> new PuRecipientNoPIIDTO(dataCipherService.hash(puRecipient.getRecipient().getTaxId()), puRecipient.getPuPayments())).toList();
    noPII.setRecipients(recipients);

    return noPII;
  }

  @Override
  protected SendNotificationPIIDTO extractPiiDto(SendNotification fullDTO) {
    SendNotificationPIIDTO piidto = new SendNotificationPIIDTO();
    piidto.setPuRecipients(fullDTO.getPuRecipients());
    return piidto;
  }

  @Override
  public SendNotification map(SendNotificationNoPII noPii) {
    SendNotificationPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), SendNotificationPIIDTO.class);

    SendNotification sendNotification = new SendNotification();
    sendNotification.setSendNotificationId(noPii.getSendNotificationId());
    sendNotification.setOrganizationId(noPii.getOrganizationId());
    sendNotification.setPaProtocolNumber(noPii.getPaProtocolNumber());
    sendNotification.setPuRecipients(pii.getPuRecipients());
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
    sendNotification.setLegalFacts(noPii.getLegalFacts());
    sendNotification.setNoPII(noPii);

    return sendNotification;
  }
}
