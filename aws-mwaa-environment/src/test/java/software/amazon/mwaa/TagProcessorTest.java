// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link TagProcessor}.
 */
class TagProcessorTest {

    @Test
    public void removeInternalTags() {
        // given
        Map<String, String> tags = ImmutableMap.of(
                "A", "1",
                "aws:tag:domain", "beta",
                "aws:vendor:x", "y",
                "C", "3");

        // when
        final Map<String, String> result = TagProcessor.removeInternalTags(tags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.containsKey("aws:tag:domain")).isFalse();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get("A")).isEqualTo("1");
        assertThat(result.get("C")).isEqualTo("3");
    }

    @Test
    public void removeInternalTagsNull() {
        // when
        final Map<String, String> result = TagProcessor.removeInternalTags(null);

        // then
        assertThat(result).isEmpty();
    }


    @Test
    public void getTagsToAdd() {
        // given
        Map<String, String> currentTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3");
        Map<String, String> desiredTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "30",
                "D", "4");
        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Map<String, String> result = processor.getTagsToAdd(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get("C")).isEqualTo("30");
        assertThat(result.get("D")).isEqualTo("4");
    }

    @Test
    public void equalMaps() {
        // given
        Map<String, String> map = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3");
        final TagProcessor processor = new TagProcessor(map);

        // when
        final Map<String, String> adds = processor.getTagsToAdd(map);
        final Collection<String> removes = processor.getTagsToRemove(map);

        // then
        assertThat(adds).isEmpty();
        assertThat(removes).isEmpty();
    }

    @Test
    public void getTagsToAddForNullCurrent() {
        // given
        Map<String, String> desiredTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(null);

        // when
        final Map<String, String> result = processor.getTagsToAdd(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(desiredTags.size());
        assertThat(result.get("A")).isEqualTo("1");
        assertThat(result.get("B")).isEqualTo("2");
        assertThat(result.get("C")).isEqualTo("3");
        assertThat(result.get("D")).isEqualTo("4");
    }

    @Test
    public void getTagsToAddForEmptyCurrent() {
        // given
        Map<String, String> desiredTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(Collections.emptyMap());

        // when
        final Map<String, String> result = processor.getTagsToAdd(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(desiredTags.size());
        assertThat(result.get("A")).isEqualTo("1");
        assertThat(result.get("B")).isEqualTo("2");
        assertThat(result.get("C")).isEqualTo("3");
        assertThat(result.get("D")).isEqualTo("4");
    }

    @Test
    public void getTagsToAddForNullDesired() {
        // given
        Map<String, String> currentTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Map<String, String> result = processor.getTagsToAdd(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTagsToAddForEmptyDesired() {
        // given
        Map<String, String> currentTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Map<String, String> result = processor.getTagsToAdd(Collections.emptyMap());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTagsToAddForNullDesiredValues() {
        // given
        Map<String, String> currentTags = new HashMap<>();
        currentTags.put("A", "1");
        currentTags.put("B", "2");
        currentTags.put("C", null);

        Map<String, String> desiredTags = new HashMap<>();
        desiredTags.put("A", "1");
        desiredTags.put("B", null);
        desiredTags.put("D", "3");

        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Map<String, String> result = processor.getTagsToAdd(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.containsKey("B")).isTrue();
        assertThat(result.get("B")).isNull();
        assertThat(result.get("D")).isEqualTo("3");
    }

    @Test
    public void getTagsToAddForAllNullDesiredValues() {
        // given
        Map<String, String> currentTags = new HashMap<>();
        currentTags.put("A", "1");
        currentTags.put("B", "2");
        currentTags.put("C", null);

        Map<String, String> desiredTags = new HashMap<>();
        desiredTags.put("A", null);
        desiredTags.put("B", null);
        desiredTags.put("C", null);

        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Map<String, String> result = processor.getTagsToAdd(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.containsKey("A")).isTrue();
        assertThat(result.containsKey("B")).isTrue();
        assertThat(result.get("A")).isNull();
        assertThat(result.get("B")).isNull();
    }

    @Test
    public void getTagsToRemoveForAllNullDesiredValues() {
        // given
        Map<String, String> currentTags = new HashMap<>();
        currentTags.put("A", "1");
        currentTags.put("B", "2");
        currentTags.put("C", null);

        Map<String, String> desiredTags = new HashMap<>();
        desiredTags.put("A", null);
        desiredTags.put("B", null);
        desiredTags.put("C", null);

        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Collection<String> result = processor.getTagsToRemove(desiredTags);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTagsToAddForAllNullCurrentValues() {
        // given
        Map<String, String> currentTags = new HashMap<>();
        currentTags.put("A", null);
        currentTags.put("B", null);

        Map<String, String> desiredTags = new HashMap<>();
        desiredTags.put("A", "1");
        desiredTags.put("B", null);

        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Map<String, String> result = processor.getTagsToAdd(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get("A")).isEqualTo("1");
    }

    @Test
    public void getTagsToRemoveForAllNullCurrentValues() {
        // given
        Map<String, String> currentTags = new HashMap<>();
        currentTags.put("A", null);
        currentTags.put("B", null);

        Map<String, String> desiredTags = new HashMap<>();
        desiredTags.put("A", "1");
        desiredTags.put("B", null);

        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Collection<String> result = processor.getTagsToRemove(desiredTags);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTagsToRemove() {
        // given
        Map<String, String> currentTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3");
        Map<String, String> desiredTags = ImmutableMap.of(
                "A", "1",
                "B", "20",
                "D", "4");
        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Collection<String> result = processor.getTagsToRemove(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.contains("C")).isTrue();
    }

    @Test
    public void getTagsToRemoveForNullCurrent() {
        // given
        Map<String, String> desiredTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(null);

        // when
        final Collection<String> result = processor.getTagsToRemove(desiredTags);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTagsToRemoveForEmptyCurrent() {
        // given
        Map<String, String> desiredTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(Collections.emptyMap());

        // when
        final Collection<String> result = processor.getTagsToRemove(desiredTags);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTagsToRemoveForNullDesired() {
        // given
        Map<String, String> currentTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Collection<String> result = processor.getTagsToRemove(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.contains("A")).isTrue();
        assertThat(result.contains("B")).isTrue();
        assertThat(result.contains("C")).isTrue();
        assertThat(result.contains("D")).isTrue();
    }

    @Test
    public void getTagsToRemoveForNullDesiredValues() {
        // given
        Map<String, String> currentTags = new HashMap<>();
        currentTags.put("A", "1");
        currentTags.put("B", "2");
        currentTags.put("C", null);

        Map<String, String> desiredTags = new HashMap<>();
        desiredTags.put("A", "1");
        desiredTags.put("B", null);
        desiredTags.put("D", "3");

        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Collection<String> result = processor.getTagsToRemove(desiredTags);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.contains("C")).isTrue();
    }

    @Test
    public void getTagsToRemoveForEmptyDesired() {
        // given
        Map<String, String> currentTags = ImmutableMap.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4");
        final TagProcessor processor = new TagProcessor(currentTags);

        // when
        final Collection<String> result = processor.getTagsToRemove(Collections.emptyMap());

        // then
        assertThat(result).isNotNull();
        assertThat(result.contains("A")).isTrue();
        assertThat(result.contains("B")).isTrue();
        assertThat(result.contains("C")).isTrue();
        assertThat(result.contains("D")).isTrue();
    }

    @Test
    public void nullMaps() {
        // given
        final TagProcessor processor = new TagProcessor(null);

        // when
        final Map<String, String> adds = processor.getTagsToAdd(null);
        final Collection<String> removes = processor.getTagsToRemove(null);

        // then
        assertThat(adds).isEmpty();
        assertThat(removes).isEmpty();
    }

    @Test
    public void emptyMaps() {
        // given
        final TagProcessor processor = new TagProcessor(Collections.emptyMap());

        // when
        final Map<String, String> adds = processor.getTagsToAdd(Collections.emptyMap());
        final Collection<String> removes = processor.getTagsToRemove(Collections.emptyMap());

        // then
        assertThat(adds).isEmpty();
        assertThat(removes).isEmpty();
    }
}
