package com.atchurey.tools.talkativebot.core.channel;

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

    /**
     * Resolves a start request for the selected scope.
     *
     * <p>Unscoped resolvers are treated as default-scope resolvers. Custom scopes require
     * {@link ScopedConversationStartResolver} so a routed message cannot accidentally start an
     * unrelated default conversation.</p>
     *
     * @param message inbound message to resolve
     * @param scope scope selected by the message router
     * @return first matching start request after scope filtering
     */
    public Optional<ConversationStartRequest> resolve(
            IncomingMessage message,
            ConversationScope scope
    ) {
        ConversationScope selectedScope = scope == null ? ConversationScope.DEFAULT : scope;

        return resolvers.stream()
                .filter(resolver -> supportsScope(resolver, selectedScope))
                .map(resolver -> resolver.resolve(message))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private boolean supportsScope(
            ConversationStartResolver resolver,
            ConversationScope scope
    ) {
        if (resolver instanceof ScopedConversationStartResolver scopedResolver) {
            return scopedResolver.supportsScope(scope);
        }

        return ConversationScope.DEFAULT.equals(scope);
    }

    private int order(ConversationStartResolver resolver) {
        if (resolver instanceof OrderedConversationStartResolver ordered) {
            return ordered.getOrder();
        }

        return 0;
    }
}
