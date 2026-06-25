package it.gov.pagopa.pu.send.model.view;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Entity
@AllArgsConstructor
@Document("campaign")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class CampaignIdView {
  @Id
  private String campaignId;
}
