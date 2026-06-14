package com.atchurey.tools.talkativebot.core.channel;

import java.util.Optional;

/**
 * Read-only routing helper exposed to {@link ConversationMessageRouter} implementations.
 *
 * <p>Routers can inspect pending interactions before choosing a {@link ConversationRoute}, without
 * owning persistence details or mutating conversation state.</p>
 */
public interface ConversationRoutingContext {

    /**
     * Finds the pending interaction for an address and internal workflow scope.
     *
     * @param address external conversation address to inspect
     * @param scope internal workflow lane to inspect
     * @return pending interaction when one exists for that address/scope pair
     */
    Optional<PendingInteraction> findPendingInteraction(
            ConversationAddress address,
            ConversationScope scope
    );
}
