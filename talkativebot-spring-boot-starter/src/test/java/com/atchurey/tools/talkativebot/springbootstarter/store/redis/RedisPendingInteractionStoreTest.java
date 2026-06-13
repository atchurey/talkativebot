package com.atchurey.tools.talkativebot.springbootstarter.store.redis;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class RedisPendingInteractionStoreTest {

    private final RedisOperations<String, Object> redisOperations = mock(RedisOperations.class);
    private final ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    private final RedisPendingInteractionStore store = new RedisPendingInteractionStore(redisOperations, new ObjectMapper());

    @Test
    void shouldUseCompatibleDefaultScopeKey() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        PendingInteraction interaction = pendingInteraction(address);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);

        store.save(interaction);

        verify(valueOperations).set("talkativebot:pending-interaction:web:session:session", interaction);
    }

    @Test
    void shouldUseScopedKeyForNonDefaultScope() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        ConversationScope scope = ConversationScope.of("checkout");
        PendingInteraction interaction = pendingInteraction(address);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);

        store.save(interaction, scope, Duration.ofMinutes(5));

        verify(valueOperations).set(
                "talkativebot:pending-interaction:web:session:session|scope:checkout",
                interaction,
                Duration.ofMinutes(5)
        );
    }

    @Test
    void shouldFindByScopedKey() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        ConversationScope scope = ConversationScope.of("checkout");
        when(redisOperations.opsForValue()).thenReturn(valueOperations);

        store.findByAddress(address, scope);

        verify(valueOperations).get("talkativebot:pending-interaction:web:session:session|scope:checkout");
    }

    @Test
    void shouldDeleteByScopedKey() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        ConversationScope scope = ConversationScope.of("checkout");

        store.deleteByAddress(address, scope);

        verify(redisOperations).delete("talkativebot:pending-interaction:web:session:session|scope:checkout");
    }

    private PendingInteraction pendingInteraction(ConversationAddress address) {
        return new PendingInteraction(address, "type", "topic", Question.text("question"), new Facts());
    }
}
