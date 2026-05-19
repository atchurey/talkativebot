package com.atchurey.tools.talkativebot.core.conversation;

public interface ConversationRuntimeInitializerResolver {

    ConversationRuntimeInitializer resolve(Class<? extends Conversation<?>> conversationType);
}