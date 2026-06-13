package com.atchurey.tools.talkativebot.springbootstarter.store.redis;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionKey;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class RedisPendingInteractionStore implements PendingInteractionStore {

    private static final Logger logger = LoggerFactory.getLogger(RedisPendingInteractionStore.class);

    private static final String KEY_PREFIX = "talkativebot:pending-interaction:";

    private final RedisOperations<String, Object> redisOperations;
    private final ObjectMapper objectMapper;

    public RedisPendingInteractionStore(
            RedisOperations<String, Object> redisOperations,
            ObjectMapper objectMapper) {
        this.redisOperations = Objects.requireNonNull(redisOperations, "redisOperations must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public void save(PendingInteraction interaction) {
        save(interaction, ConversationScope.DEFAULT);
    }

    @Override
    public void save(
            PendingInteraction interaction,
            ConversationScope scope
    ) {
        Objects.requireNonNull(interaction, "interaction must not be null");

        String key = key(interaction.getAddress(), scope);
        redisOperations.opsForValue().set(key, interaction);

        logger.debug("Saved pending interaction {} in Redis for {}", interaction.getId(), key);
    }

    @Override
    public void save(PendingInteraction interaction, Duration ttl) {
        save(interaction, ConversationScope.DEFAULT, ttl);
    }

    @Override
    public void save(
            PendingInteraction interaction,
            ConversationScope scope,
            Duration ttl
    ) {
        Objects.requireNonNull(interaction, "interaction must not be null");

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            save(interaction, scope);
            return;
        }

        String key = key(interaction.getAddress(), scope);
        redisOperations.opsForValue().set(key, interaction, ttl);

        logger.debug(
                "Saved pending interaction {} in Redis for {} with ttl {}",
                interaction.getId(),
                key,
                ttl
        );
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

        Object value = redisOperations.opsForValue().get(key(address, scope));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(toPendingInteraction(value, address));
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

        String key = key(address, scope);
        redisOperations.delete(key);

        logger.debug("Deleted pending interaction from Redis for {}", key);
    }

    private PendingInteraction toPendingInteraction(Object value, ConversationAddress address) {
        if (value instanceof PendingInteraction pendingInteraction) {
            return pendingInteraction;
        }

        try {
            return objectMapper.convertValue(value, PendingInteraction.class);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Redis value for address " + address.persistenceKey()
                            + " could not be converted to PendingInteraction. Actual type: "
                            + value.getClass().getName(),
                    exception
            );
        }
    }

    private String key(
            ConversationAddress address,
            ConversationScope scope
    ) {
        return KEY_PREFIX + PendingInteractionKey.from(address, scope);
    }
}
