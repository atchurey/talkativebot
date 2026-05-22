package com.atchurey.tools.talkativebot.core.conversation;

public interface ConversationRuntime  extends AutoCloseable{

    Object get(String key);

    <T> T get(String key, Class<T> type);

    boolean contains(String key);

    @Override
    default void close() {
    }

}