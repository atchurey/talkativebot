package com.atchurey.tools.talkativebot.core.conversation;

import java.util.NoSuchElementException;

public final class EmptyConversationRuntime implements ConversationRuntime {

    public static final EmptyConversationRuntime INSTANCE =
            new EmptyConversationRuntime();

    private EmptyConversationRuntime() {
    }

    @Override
    public Object get(String key) {
        throw new NoSuchElementException("No runtime resource found for key: " + key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        throw new NoSuchElementException("No runtime resource found for key: " + key);
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

}