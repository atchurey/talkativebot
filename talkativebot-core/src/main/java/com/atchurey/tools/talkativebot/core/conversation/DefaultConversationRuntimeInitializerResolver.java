package com.atchurey.tools.talkativebot.core.conversation;

import java.util.Collections;
import java.util.List;

public class DefaultConversationRuntimeInitializerResolver implements ConversationRuntimeInitializerResolver {

    public static final DefaultConversationRuntimeInitializerResolver INSTANCE =
            new DefaultConversationRuntimeInitializerResolver(Collections.emptyList());

    private final List<ConversationRuntimeInitializer> initializers;

    public DefaultConversationRuntimeInitializerResolver(List<ConversationRuntimeInitializer> initializers) {
        this.initializers = initializers == null ? Collections.emptyList() : initializers;
    }

    @Override
    public ConversationRuntimeInitializer resolve(Class<? extends Conversation<?>> conversationType) {
        return initializers.stream()
                .filter(initializer -> initializer.supports(conversationType))
                .findFirst()
                .orElse(DefaultConversationRuntimeInitializer.INSTANCE);
    }
}