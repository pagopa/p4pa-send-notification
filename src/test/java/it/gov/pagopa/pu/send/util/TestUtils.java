package it.gov.pagopa.pu.send.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;

public class TestUtils {
    private TestUtils(){}

    /**
     * It will assert not null on all o's fields
     */
    public static void checkNotNullFields(Object o, String... excludedFields) {
        Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
        org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
                f -> {
                    f.setAccessible(true);
                    Assertions.assertNotNull(f.get(o), "The field "+f.getName()+" of the input object of type "+o.getClass()+" is null!");
                },
                f -> !excludedFieldsSet.contains(f.getName()));
    }

}
