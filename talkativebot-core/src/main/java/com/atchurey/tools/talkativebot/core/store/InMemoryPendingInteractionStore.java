package com.atchurey.tools.talkativebot.core.store;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionKey;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryPendingInteractionStore implements PendingInteractionStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryPendingInteractionStore.class);

    private final ConcurrentMap<String, StoredPendingInteraction> interactions = new ConcurrentHashMap<>();

    @Override
    public void save(PendingInteraction interaction) {
        save(interaction, ConversationScope.DEFAULT);
    }

    @Override
    public void save(
            PendingInteraction interaction,
            ConversationScope scope
    ) {
        saveInternal(interaction, scope, null);
    }

    @Override
    public void save(PendingInteraction interaction, Duration ttl) {
        Instant expiresAt = ttl == null || ttl.isZero() || ttl.isNegative()
                ? null
                : Instant.now().plus(ttl);

        saveInternal(interaction, ConversationScope.DEFAULT, expiresAt);
    }

    @Override
    public void save(
            PendingInteraction interaction,
            ConversationScope scope,
            Duration ttl
    ) {
        Instant expiresAt = ttl == null || ttl.isZero() || ttl.isNegative()
                ? null
                : Instant.now().plus(ttl);

        saveInternal(interaction, scope, expiresAt);
    }

    @Override
    public Optional<PendingInteraction> findByAddress(ConversationAddress address) {
        return findByAddress(address, ConversationScope.DEFAULT);
    }

    @Override
    public Optional<PendingInteraction> findByAddress(
            ConversationAddress address,
            ConversationScope scope
    ) {
        Objects.requireNonNull(address, "address must not be null");

        String key = PendingInteractionKey.from(address, scope);
        StoredPendingInteraction storedInteraction = interactions.get(key);

        if (storedInteraction == null) {
            return Optional.empty();
        }

        if (storedInteraction.isExpired()) {
            interactions.remove(key);
            logger.debug("Removed expired pending interaction for {}", key);
            return Optional.empty();
        }

        return Optional.of(storedInteraction.interaction());
    }

    @Override
    public void deleteByAddress(ConversationAddress address) {
        deleteByAddress(address, ConversationScope.DEFAULT);
    }

    @Override
    public void deleteByAddress(
            ConversationAddress address,
            ConversationScope scope
    ) {
        Objects.requireNonNull(address, "address must not be null");

        String key = PendingInteractionKey.from(address, scope);
        interactions.remove(key);

        logger.debug("Deleted pending interaction from memory for {}", key);
    }

    private void saveInternal(
            PendingInteraction interaction,
            ConversationScope scope,
            Instant expiresAt
    ) {
        Objects.requireNonNull(interaction, "interaction must not be null");

        String key = PendingInteractionKey.from(interaction.getAddress(), scope);
        interactions.put(key, new StoredPendingInteraction(interaction, expiresAt));

        logger.debug(
                "Saved pending interaction {} in memory for {}",
                interaction.getId(),
                key
        );
    }

    private record StoredPendingInteraction(
            PendingInteraction interaction,
            Instant expiresAt
    ) {

        private boolean isExpired() {
            return expiresAt != null && Instant.now().isAfter(expiresAt);
        }
    }
}
