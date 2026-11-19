package it.gov.pagopa.pu.send.util;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class OrgSubUnitCriteriaUtils {
  private OrgSubUnitCriteriaUtils() {
  }

  /**
   * Restrict the query on the given orgSubUnitCode field:
   * <ul>
   *   <li>orgSubUnitCodes not empty and fetchAll true: field IN orgSubUnitCodes OR field null (organization and subUnits level)</li>
   *   <li>orgSubUnitCodes not empty and fetchAll not true: field IN orgSubUnitCodes (subUnits level only)</li>
   *   <li>orgSubUnitCodes empty: field null (organization level only)</li>
   * </ul>
   */
  public static void addOrgSubUnitCriteria(Query query, String orgSubUnitCodeField, List<String> orgSubUnitCodes, Boolean fetchAll) {
    if (!CollectionUtils.isEmpty(orgSubUnitCodes)) {
      if (Boolean.TRUE.equals(fetchAll)) {
        query.addCriteria(new Criteria().orOperator(
          Criteria.where(orgSubUnitCodeField).in(orgSubUnitCodes),
          Criteria.where(orgSubUnitCodeField).isNull()
        ));
      } else {
        query.addCriteria(Criteria.where(orgSubUnitCodeField).in(orgSubUnitCodes));
      }
    } else {
      query.addCriteria(Criteria.where(orgSubUnitCodeField).isNull());
    }
  }
}
