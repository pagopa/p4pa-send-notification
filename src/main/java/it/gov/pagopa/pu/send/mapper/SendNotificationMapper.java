package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.citizen.service.DataCipherService;
import it.gov.pagopa.pu.send.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.dto.SendNotificationPIIDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationMapper extends BasePIIMapper<SendNotification, SendNotificationNoPII, SendNotificationPIIDTO> {

  private final DataCipherService dataCipherService;
  private final PersonalDataService personalDataService;

  public SendNotificationMapper(DataCipherService dataCipherService,
    PersonalDataService personalDataService) {
    this.dataCipherService = dataCipherService;
    this.personalDataService = personalDataService;
  }

  @Override
  protected SendNotificationNoPII extractNoPiiEntity(SendNotification fullDTO) {
    SendNotificationNoPII noPII = new SendNotificationNoPII();

    noPII.setSendNotificationId(fullDTO.getSendNotificationId());
    noPII.setOrganizationId(fullDTO.getOrganizationId());
    noPII.setPaProtocolNumber(fullDTO.getPaProtocolNumber());
    noPII.setSubjectType(fullDTO.getSubjectType());
    noPII.setFiscalCodeHash(dataCipherService.hash(fullDTO.getFiscalCode()));
    noPII.setDenomination(fullDTO.getDenomination());
    noPII.setPayments(fullDTO.getPayments());
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
    noPII.setNotificationData(fullDTO.getNotificationData());

    return noPII;
  }

  @Override
  protected SendNotificationPIIDTO extractPiiDto(SendNotification fullDTO) {
    return SendNotificationPIIDTO.builder()
      .fiscalCode(fullDTO.getFiscalCode())
      .address(fullDTO.getAddress()).build();
  }

  @Override
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
