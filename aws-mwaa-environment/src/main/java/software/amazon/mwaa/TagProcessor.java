// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides methods to process Tags in memory before submitting them to APIs.
 */
public class TagProcessor {
    private static final String INTERNAL_TAG_PREFIX = "aws:";

    private final Map<String, String> currentTags;

    /**
     * Constructor.
     *
     * @param currentTags
     *         current tags on the resource
     */
    public TagProcessor(Map<String, String> currentTags) {
        this.currentTags = removeInternalTags(currentTags);
    }

    /**
     * Removes tags which are internal to AWS from tags.
     *
     * @param tags
     *         tags map
     * @return tags without internal ones
     */
    public static Map<String, String> removeInternalTags(Map<String, String> tags) {
        if (tags == null) {
            return Collections.emptyMap();
        }

        final Map<String, String> result = new HashMap<>();
        tags.entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith(INTERNAL_TAG_PREFIX))
                // use forEach instead of Collectors.toMap to allow null values
                .forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * Returns key and values for tags to be added to resource.
     * <p>
     * This contains all missing entries plus entries with different values.
     *
     * @param desiredTags
     *         desired tags
     * @return a map (key/value) of new tags and the ones with updated values
     */
    public Map<String, String> getTagsToAdd(final Map<String, String> desiredTags) {
        if (desiredTags == null || desiredTags.isEmpty()) {
            return Collections.emptyMap();
        }

        if (currentTags.isEmpty()) {
            return desiredTags;
        }

        final Map<String, String> result = new HashMap<>();
        getKeysWithDifferentValues(desiredTags).forEach(key -> result.put(key, desiredTags.get(key)));
        getKeysInDesiredNotInCurrent(desiredTags).forEach(key -> result.put(key, desiredTags.get(key)));

        return result;
    }

    /**
     * Returns keys for tags to be removed from resource.
     *
     * @param desiredTags
     *         desired tags
     * @return a collection of keys to be removed from resource
     */
    public Collection<String> getTagsToRemove(final Map<String, String> desiredTags) {
        if (desiredTags == null || desiredTags.isEmpty() || currentTags.isEmpty()) {
            return currentTags.keySet();
        }

        return getKeysInCurrentNotInDesired(desiredTags);
    }

    private Set<String> getKeysWithDifferentValues(final Map<String, String> desiredTags) {
        return currentTags.keySet()
                .stream()
                .filter(desiredTags::containsKey)
                .filter(key -> areDifferent(currentTags.get(key), desiredTags.get(key)))
                .collect(Collectors.toSet());
    }

    private Set<String> getKeysInDesiredNotInCurrent(final Map<String, String> desiredTags) {
        return desiredTags.keySet()
                .stream()
                .filter(key -> !currentTags.containsKey(key))
                .collect(Collectors.toSet());
    }

    private Set<String> getKeysInCurrentNotInDesired(final Map<String, String> desiredTags) {
        return currentTags.keySet()
                .stream()
                .filter(key -> !desiredTags.containsKey(key))
                .collect(Collectors.toSet());
    }

    private static boolean areDifferent(final String a, final String b) {
        return (a == null && b != null)
                || (b == null && a != null)
                || (a != null && !a.equals(b));
    }

}
