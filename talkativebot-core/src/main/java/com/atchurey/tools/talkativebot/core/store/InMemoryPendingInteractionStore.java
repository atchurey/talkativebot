package com.atchurey.talkativebot.core.store;

import com.atchurey.talkativebot.core.channel.ConversationAddress;
import com.atchurey.talkativebot.core.channel.PendingInteraction;
import com.atchurey.talkativebot.core.channel.PendingInteractionStore;
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
        Objects.requireNonNull(interaction, "interaction must not be null");

        interactions.put(
                interaction.getAddress().persistenceKey(),
                new StoredPendingInteraction(interaction, null)
        );

        logger.debug(
                "Saved pending interaction {} in memory for {}",
                interaction.getId(),
                interaction.getAddress().persistenceKey()
        );
    }

    @Override
    public void save(PendingInteraction interaction, Duration ttl) {
        Objects.requireNonNull(interaction, "interaction must not be null");

        Instant expiresAt = ttl == null || ttl.isZero() || ttl.isNegative()
                ? null
                : Instant.now().plus(ttl);

        interactions.put(
                interaction.getAddress().persistenceKey(),
                new StoredPendingInteraction(interaction, expiresAt)
        );

        logger.debug(
                "Saved pending interaction {} in memory for {} with ttl {}",
                interaction.getId(),
                interaction.getAddress().persistenceKey(),
                ttl
        );
    }

    @Override
    public Optional<PendingInteraction> findByAddress(ConversationAddress address) {
        Objects.requireNonNull(address, "address must not be null");

        String key = address.persistenceKey();
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
        Objects.requireNonNull(address, "address must not be null");

        String key = address.persistenceKey();
        interactions.remove(key);

        logger.debug("Deleted pending interaction from memory for {}", key);
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
