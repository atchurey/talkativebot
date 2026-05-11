package com.atchurey.talkativebot.springbootstarter.store.redis;

import com.atchurey.talkativebot.core.channel.ConversationAddress;
import com.atchurey.talkativebot.core.channel.PendingInteraction;
import com.atchurey.talkativebot.core.channel.PendingInteractionStore;
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
        Objects.requireNonNull(interaction, "interaction must not be null");

        redisOperations.opsForValue().set(key(interaction.getAddress()), interaction);

        logger.debug(
                "Saved pending interaction {} in Redis for {}",
                interaction.getId(),
                interaction.getAddress().persistenceKey()
        );
    }

    @Override
    public void save(PendingInteraction interaction, Duration ttl) {
        Objects.requireNonNull(interaction, "interaction must not be null");

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            save(interaction);
            return;
        }

        redisOperations.opsForValue().set(key(interaction.getAddress()), interaction, ttl);

        logger.debug(
                "Saved pending interaction {} in Redis for {} with ttl {}",
                interaction.getId(),
                interaction.getAddress().persistenceKey(),
                ttl
        );
    }

    @Override
    public Optional<PendingInteraction> findByAddress(ConversationAddress address) {
        Objects.requireNonNull(address, "address must not be null");

        Object value = redisOperations.opsForValue().get(key(address));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(toPendingInteraction(value, address));
    }

    @Override
    public void deleteByAddress(ConversationAddress address) {
        Objects.requireNonNull(address, "address must not be null");

        redisOperations.delete(key(address));

        logger.debug("Deleted pending interaction from Redis for {}", address.persistenceKey());
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

    private String key(ConversationAddress address) {
        return KEY_PREFIX + address.persistenceKey();
    }
}