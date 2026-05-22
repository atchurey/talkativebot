package com.atchurey.tools.talkativebot.core.conversation;

public class DefaultConversationRuntimeInitializer
        implements ConversationRuntimeInitializer {

    public static final DefaultConversationRuntimeInitializer INSTANCE =
            new DefaultConversationRuntimeInitializer();

    private DefaultConversationRuntimeInitializer() {
    }

    @Override
    public boolean supports(Class<? extends Conversation<?>> conversationType) {
        return true;
    }

    @Override
    public ConversationRuntime initialize(Class<? extends Conversation<?>> conversationType) {
       return EmptyConversationRuntime.INSTANCE;
    }
}