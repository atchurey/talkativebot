package com.atchurey.tools.talkativebot.core.channel;

import java.time.Duration;
import java.util.Optional;

/**
 * Persists pending questions while TalkativeBot waits for an external reply.
 * <p>
 * The original address-only methods remain the default-scope API. Store
 * implementations that support parallel workflows should override the scoped
 * methods so multiple {@link ConversationScope}s can coexist for the same
 * {@link ConversationAddress}.
 */
public interface PendingInteractionStore {

    /**
     * Saves a pending interaction in the default scope.
     */
    void save(PendingInteraction interaction);

    /**
     * Saves a pending interaction in a specific workflow scope.
     * <p>
     * The default implementation preserves source compatibility for custom
     * stores: default scope delegates to {@link #save(PendingInteraction)}, while
     * non-default scopes fail until the store explicitly supports scoped keys.
     */
    default void save(
            PendingInteraction interaction,
            ConversationScope scope
    ) {
        if (ConversationScope.DEFAULT.equals(scope) || scope == null) {
            save(interaction);
            return;
        }

        throw new UnsupportedOperationException("Scoped pending interactions are not supported by this store");
    }

    /**
     * Saves a pending interaction in the default scope with an optional TTL.
     */
    void save(PendingInteraction interaction, Duration ttl);

    /**
     * Saves a pending interaction in a specific workflow scope with an optional
     * TTL.
     * <p>
     * The default implementation preserves source compatibility for custom
     * stores: default scope delegates to {@link #save(PendingInteraction, Duration)},
     * while non-default scopes fail until the store explicitly supports scoped
     * keys.
     */
    default void save(
            PendingInteraction interaction,
            ConversationScope scope,
            Duration ttl
    ) {
        if (ConversationScope.DEFAULT.equals(scope) || scope == null) {
            save(interaction, ttl);
            return;
        }

        throw new UnsupportedOperationException("Scoped pending interactions are not supported by this store");
    }

    /**
     * Finds the default-scope pending interaction for an address.
     */
    Optional<PendingInteraction> findByAddress(ConversationAddress address);

    /**
     * Finds the pending interaction for an address/scope pair.
     * <p>
     * The default implementation preserves source compatibility for custom
     * stores: default scope delegates to {@link #findByAddress(ConversationAddress)},
     * while non-default scopes fail until the store explicitly supports scoped
     * keys.
     */
    default Optional<PendingInteraction> findByAddress(
            ConversationAddress address,
            ConversationScope scope
    ) {
        if (ConversationScope.DEFAULT.equals(scope) || scope == null) {
            return findByAddress(address);
        }

        throw new UnsupportedOperationException("Scoped pending interactions are not supported by this store");
    }

    /**
     * Deletes the default-scope pending interaction for an address.
     */
    void deleteByAddress(ConversationAddress address);

    /**
     * Deletes the pending interaction for an address/scope pair.
     * <p>
     * The default implementation preserves source compatibility for custom
     * stores: default scope delegates to {@link #deleteByAddress(ConversationAddress)},
     * while non-default scopes fail until the store explicitly supports scoped
     * keys.
     */
    default void deleteByAddress(
            ConversationAddress address,
            ConversationScope scope
    ) {
        if (ConversationScope.DEFAULT.equals(scope) || scope == null) {
            deleteByAddress(address);
            return;
        }

        throw new UnsupportedOperationException("Scoped pending interactions are not supported by this store");
    }
}
