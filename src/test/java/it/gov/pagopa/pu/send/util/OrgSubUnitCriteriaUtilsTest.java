package it.gov.pagopa.pu.send.util;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;

class OrgSubUnitCriteriaUtilsTest {

  private static final String ORG_SUB_UNIT_CODE_FIELD = "orgSubUnitCode";

  @Test
  void givenCodesAndFetchAllWhenAddOrgSubUnitCriteriaThenCodesOrNull() {
    // Given
    Query query = new Query();
    List<String> codes = List.of("CODE1", "CODE2");

    // When
    OrgSubUnitCriteriaUtils.addOrgSubUnitCriteria(query, ORG_SUB_UNIT_CODE_FIELD, codes, true);

    // Then
    Assertions.assertEquals(
      new Document("$or", Arrays.asList(
        new Document(ORG_SUB_UNIT_CODE_FIELD, new Document("$in", codes)),
        new Document(ORG_SUB_UNIT_CODE_FIELD, null)
      )),
      query.getQueryObject());
  }

  @Test
  void givenCodesAndNotFetchAllWhenAddOrgSubUnitCriteriaThenCodesOnly() {
    // Given
    Query query = new Query();
    List<String> codes = List.of("CODE1");

    // When
    OrgSubUnitCriteriaUtils.addOrgSubUnitCriteria(query, ORG_SUB_UNIT_CODE_FIELD, codes, null);

    // Then
    Assertions.assertEquals(new Document(ORG_SUB_UNIT_CODE_FIELD, new Document("$in", codes)), query.getQueryObject());
  }

  @Test
  void givenNoCodesWhenAddOrgSubUnitCriteriaThenOrganizationLevelOnly() {
    // Given
    Query query = new Query();

    // When
    OrgSubUnitCriteriaUtils.addOrgSubUnitCriteria(query, ORG_SUB_UNIT_CODE_FIELD, null, true);

    // Then
    Assertions.assertEquals(new Document(ORG_SUB_UNIT_CODE_FIELD, null), query.getQueryObject());
  }
}
