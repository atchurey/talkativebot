package com.atchurey.tools.talkativebot.core.conversation;

public interface ConversationRuntimeInitializer {

    boolean supports(Class<? extends Conversation<?>> conversationType);

    ConversationRuntime initialize(Class<? extends Conversation<?>> conversationType);
}