// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TypeTranslator}.
 */
class TypeTranslatorTest {

    private static final String KEY_1 = "KEY_1";
    private static final String KEY_2 = "KEY_2";
    private static final String KEY_3 = "KEY_3";
    private static final Object VALUE_1 = "VALUE1";
    private static final String VALUE_2 = "A\"B";

    @Test
    public void nullInputs() {
        assertThat(TypeTranslator.toApiLoggingConfiguration(null)).isNull();
        assertThat(TypeTranslator.toApiModuleLoggingConfigurationInput(null)).isNull();
        assertThat(TypeTranslator.toApiNetworkConfiguration(null)).isNull();
        assertThat(TypeTranslator.toCfnLoggingConfiguration(null)).isNull();
        assertThat(TypeTranslator.toCfnModuleLoggingConfiguration(null)).isNull();
        assertThat(TypeTranslator.toCfnNetworkConfiguration(null)).isNull();
        assertThat(TypeTranslator.toStringToObjectMap(null)).isNull();
        assertThat(TypeTranslator.toStringToStringMap(null)).isNull();
        assertThat(TypeTranslator.toStreamOfOrEmpty(null)).isNotNull();
        assertThat(TypeTranslator.toStreamOfOrEmpty(null).count()).isEqualTo(0);
        assertThat(TypeTranslator.mapToLogString(null)).isEqualTo("{}");
        assertThat(TypeTranslator.collectionToLogString(null)).isEqualTo("[]");
    }

    @Test
    public void toStringToStringMapNullValue() {
        // given
        final Map<String, Object> input = new HashMap<>();
        input.put(KEY_1, VALUE_1);
        input.put(KEY_2, null);

        // when
        final Map<String, String> result = TypeTranslator.toStringToStringMap(input);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get(KEY_1)).isEqualTo(VALUE_1.toString());
        assertThat(result.get(KEY_2)).isNull();
    }

    @Test
    public void mapToLogString() {
        // given
        final Map<String, String> input = new HashMap<>();
        input.put(KEY_1, VALUE_1.toString());
        input.put(KEY_3, VALUE_2);
        input.put(KEY_2, null);

        // when
        final String result = TypeTranslator.mapToLogString(input);

        // then
        assertThat(result).isEqualTo("{KEY_1=\"VALUE1\", KEY_2=null, KEY_3=\"A\"B\"}");
    }

    @Test
    public void collectionToLogString() {
        // given
        final Collection<String> input = new ArrayList<>();
        input.add(null);
        input.add(VALUE_1.toString());
        input.add(null);
        input.add(VALUE_2);

        // when
        final String result = TypeTranslator.collectionToLogString(input);

        // then
        assertThat(result).isEqualTo("[\"A\"B\", \"VALUE1\", null, null]");
    }
}
