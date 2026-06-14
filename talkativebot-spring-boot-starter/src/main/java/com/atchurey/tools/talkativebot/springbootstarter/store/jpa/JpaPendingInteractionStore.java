package com.atchurey.tools.talkativebot.springbootstarter.store.jpa;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionKey;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class JpaPendingInteractionStore implements PendingInteractionStore {

    private static final Logger logger = LoggerFactory.getLogger(JpaPendingInteractionStore.class);

    private final JpaPendingInteractionRepository repository;
    private final ObjectMapper objectMapper;

    public JpaPendingInteractionStore(
            JpaPendingInteractionRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    @Transactional
    public void save(PendingInteraction interaction) {
        save(interaction, ConversationScope.DEFAULT);
    }

    @Override
    @Transactional
    public void save(
            PendingInteraction interaction,
            ConversationScope scope
    ) {
        saveInternal(interaction, scope, null);
    }

    @Override
    @Transactional
    public void save(PendingInteraction interaction, Duration ttl) {
        Instant expiresAt = ttl == null || ttl.isZero() || ttl.isNegative()
                ? null
                : Instant.now().plus(ttl);

        saveInternal(interaction, ConversationScope.DEFAULT, expiresAt);
    }

    @Override
    @Transactional
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
        return repository.findByAddressKey(key)
                .filter(entity -> !isExpired(entity))
                .map(this::toPendingInteraction);
    }

    @Override
    @Transactional
    public void deleteByAddress(ConversationAddress address) {
        deleteByAddress(address, ConversationScope.DEFAULT);
    }

    @Override
    @Transactional
    public void deleteByAddress(
            ConversationAddress address,
            ConversationScope scope
    ) {
        Objects.requireNonNull(address, "address must not be null");

        String key = PendingInteractionKey.from(address, scope);
        repository.deleteByAddressKey(key);

        logger.debug("Deleted pending interaction from database for {}", key);
    }

    @Transactional
    public void deleteExpired() {
        repository.deleteByExpiresAtBefore(Instant.now());
    }

    private void saveInternal(
            PendingInteraction interaction,
            ConversationScope scope,
            Instant expiresAt
    ) {
        Objects.requireNonNull(interaction, "interaction must not be null");

        ConversationAddress address = interaction.getAddress();
        ConversationScope resolvedScope = scope == null ? ConversationScope.DEFAULT : scope;
        String key = PendingInteractionKey.from(address, resolvedScope);

        PendingInteractionEntity entity = repository.findByAddressKey(key)
                .orElseGet(PendingInteractionEntity::new);

        entity.setId(interaction.getId());
        entity.setAddressKey(key);
        entity.setScope(resolvedScope.value());
        entity.setChannel(address.getChannel());
        entity.setUserId(address.getUserId());
        entity.setSessionId(address.getSessionId());
        entity.setConversationId(address.getConversationId());
        entity.setConversationType(interaction.getConversationType());
        entity.setCurrentTopicKey(interaction.getCurrentTopicKey());
        entity.setPayloadJson(toJson(interaction));
        entity.setCreatedAt(interaction.getCreatedAt());
        entity.setUpdatedAt(interaction.getUpdatedAt());
        entity.setExpiresAt(expiresAt);

        repository.save(entity);

        logger.debug(
                "Saved pending interaction {} in database for {}",
                interaction.getId(),
                key
        );
    }

    private PendingInteraction toPendingInteraction(PendingInteractionEntity entity) {
        try {
            return objectMapper.readValue(entity.getPayloadJson(), PendingInteraction.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not deserialize pending interaction " + entity.getId(),
                    exception
            );
        }
    }

    private String toJson(PendingInteraction interaction) {
        try {
            return objectMapper.writeValueAsString(interaction);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not serialize pending interaction " + interaction.getId(),
                    exception
            );
        }
    }

    private boolean isExpired(PendingInteractionEntity entity) {
        if (entity.getExpiresAt() == null) {
            return false;
        }

        boolean expired = Instant.now().isAfter(entity.getExpiresAt());

        if (expired) {
            logger.debug(
                    "Pending interaction {} for {} is expired",
                    entity.getId(),
                    entity.getAddressKey()
            );
        }

        return expired;
    }
}
