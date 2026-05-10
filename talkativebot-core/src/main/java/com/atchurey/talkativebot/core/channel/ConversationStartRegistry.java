package com.atchurey.talkativebot.core.channel;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ConversationStartRegistry {

    private final List<ConversationStartResolver> resolvers;

    public ConversationStartRegistry(List<ConversationStartResolver> resolvers) {
        this.resolvers = resolvers == null
                ? List.of()
                : resolvers.stream()
                        .sorted(Comparator.comparingInt(this::order))
                        .toList();
    }

    public Optional<ConversationStartRequest> resolve(IncomingMessage message) {
        return resolvers.stream()
                .map(resolver -> resolver.resolve(message))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private int order(ConversationStartResolver resolver) {
        if (resolver instanceof OrderedConversationStartResolver ordered) {
            return ordered.getOrder();
        }

        return 0;
    }
}
