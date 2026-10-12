package it.gov.pagopa.pu.send.util;

import java.lang.reflect.Field;
import java.util.*;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import uk.co.jemos.podam.api.AttributeMetadata;
import uk.co.jemos.podam.api.DataProviderStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.jemos.podam.common.ManufacturingContext;
import uk.co.jemos.podam.typeManufacturers.AbstractTypeManufacturer;

public class TestUtils {

  private TestUtils() {}

  static {
    clearDefaultTimezone();
    clearLocale();
  }

  public static void clearDefaultTimezone() {
    TimeZone.setDefault(Constants.DEFAULT_TIMEZONE);
  }

  public static void clearLocale() {
    Locale.setDefault(Locale.ITALY);
  }

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field " + f.getName() + " of the input object of type " + o.getClass() + " is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

  public static PodamFactory getPodamFactory() {
    PodamFactoryImpl podamFactory = new PodamFactoryImpl();
    podamFactory.getStrategy().addOrReplaceTypeManufacturer(SortedSet.class, new AbstractTypeManufacturer<>() {
      @Override
      public SortedSet<?> getType(DataProviderStrategy strategy, AttributeMetadata attributeMetadata, ManufacturingContext manufacturingCtx) {
        return new TreeSet<>();
      }
    });
    return podamFactory;
  }

  /**
   * It will assert that all non excluded fields of class 'C' are set in the upsert document
   */
  public static <C> void assertUpsertedFields(Document upsertDocument, Class<C> clazz, String... excludedFieldNames) {
    List<String> upsertedFieldNameList = getUpsertedFieldNameList(upsertDocument);
    List<Field> classFieldList = Arrays.asList(clazz.getDeclaredFields());
    List<String> excludedFieldNameList = Arrays.asList(excludedFieldNames);
    classFieldList.forEach(field ->
      Assertions.assertTrue(
        upsertedFieldNameList.contains(field.getName()) || excludedFieldNameList.contains(field.getName()),
        "The field %s of the object of type %s is neither set in the upsert document nor excluded!".formatted(field.getName(), clazz)
      )
    );
  }

  /**
   * Extract from the upsertDocument names of all upserted fields
   */
  private static List<String> getUpsertedFieldNameList(Document upsertDocument) {
    List<String> upsertedFieldNameList = new ArrayList<>();
    extractUpsertedFieldNames(upsertDocument, "$set", upsertedFieldNameList);
    extractUpsertedFieldNames(upsertDocument, "$setOnInsert", upsertedFieldNameList);
    return upsertedFieldNameList;
  }

  /**
   * Extract from the upsertDocument names of upserted fields for the specified upsertOperator ($set|$setOnInsert)
   */
  private static void extractUpsertedFieldNames(Document upsertDocument, String upsertOperator, List<String> upsertedFieldNameList) {
    List<String> partialUpsertedFieldNameList = upsertDocument.get(upsertOperator, Document.class).entrySet().stream()
      .filter(e -> e.getValue() != null)
      .map(Map.Entry::getKey)
      .toList();
    upsertedFieldNameList.addAll(partialUpsertedFieldNameList);
  }

}
