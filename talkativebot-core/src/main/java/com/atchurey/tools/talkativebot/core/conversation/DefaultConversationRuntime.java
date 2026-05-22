package com.atchurey.tools.talkativebot.core.conversation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DefaultConversationRuntime implements ConversationRuntime {

    private final Map<String, Object> resources;

    private DefaultConversationRuntime(Map<String, Object> resources) {
        this.resources = Collections.unmodifiableMap(new LinkedHashMap<>(resources));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Object get(String key) {
        if (!resources.containsKey(key)) {
            throw new NoSuchElementException("No runtime resource found for key: " + key);
        }

        return resources.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);

        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Runtime resource '" + key + "' is not of type " + type.getName()
                            + ". Actual type: " + value.getClass().getName()
            );
        }

        return type.cast(value);
    }

    @Override
    public boolean contains(String key) {
        return resources.containsKey(key);
    }

    public static class Builder {

        private final Map<String, Object> resources = new LinkedHashMap<>();

        public Builder put(String key, Object value) {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("Runtime resource key must not be blank");
            }

            if (value == null) {
                throw new IllegalArgumentException("Runtime resource value must not be null for key: " + key);
            }

            resources.put(key, value);
            return this;
        }

        public ConversationRuntime build() {
            if (resources.isEmpty()) {
                return EmptyConversationRuntime.INSTANCE;
            }

            return new DefaultConversationRuntime(resources);
        }
    }
}