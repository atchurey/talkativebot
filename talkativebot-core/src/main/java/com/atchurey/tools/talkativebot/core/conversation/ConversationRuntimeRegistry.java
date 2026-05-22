package com.atchurey.tools.talkativebot.core.conversation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConversationRuntimeRegistry implements AutoCloseable {

    private final ConcurrentMap<String, ConversationRuntime> runtimes = new ConcurrentHashMap<>();

    public ConversationRuntime getOrInitialize(
            Class<? extends Conversation<?>> conversationType,
            ConversationRuntimeInitializer initializer) {

        if (conversationType == null) {
            throw new IllegalArgumentException("conversationType must not be null");
        }

        if (initializer == null) {
            throw new IllegalArgumentException("initializer must not be null");
        }

        return runtimes.computeIfAbsent(
                conversationType.getName(),
                key -> initializer.initialize(conversationType)
        );
    }

    public boolean contains(Class<? extends Conversation<?>> conversationType) {
        return conversationType != null && runtimes.containsKey(conversationType.getName());
    }

    public void clear() {
        runtimes.clear();
    }

    @Override
    public void close() {
        for (Map.Entry<String, ConversationRuntime> entry : runtimes.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception ignored) {
                // Ignoring close failures during shutdown.
            }
        }

        runtimes.clear();
    }
}