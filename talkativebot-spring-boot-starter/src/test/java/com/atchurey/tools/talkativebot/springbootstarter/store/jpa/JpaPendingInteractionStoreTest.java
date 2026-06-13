package com.atchurey.tools.talkativebot.springbootstarter.store.jpa;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaPendingInteractionStoreTest {

    private final JpaPendingInteractionRepository repository = mock(JpaPendingInteractionRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final JpaPendingInteractionStore store = new JpaPendingInteractionStore(repository, objectMapper);

    @Test
    void shouldUseCompatibleDefaultScopeKey() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        PendingInteraction interaction = pendingInteraction(address);
        when(repository.findByAddressKey("web:session:session")).thenReturn(Optional.empty());

        store.save(interaction);

        ArgumentCaptor<PendingInteractionEntity> captor = ArgumentCaptor.forClass(PendingInteractionEntity.class);
        verify(repository).save(captor.capture());

        PendingInteractionEntity entity = captor.getValue();
        assertThat(entity.getAddressKey()).isEqualTo("web:session:session");
        assertThat(entity.getScope()).isEqualTo("default");
    }

    @Test
    void shouldUseScopedKeyForNonDefaultScope() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        ConversationScope scope = ConversationScope.of("checkout");
        PendingInteraction interaction = pendingInteraction(address);
        when(repository.findByAddressKey("web:session:session|scope:checkout")).thenReturn(Optional.empty());

        store.save(interaction, scope);

        ArgumentCaptor<PendingInteractionEntity> captor = ArgumentCaptor.forClass(PendingInteractionEntity.class);
        verify(repository).save(captor.capture());

        PendingInteractionEntity entity = captor.getValue();
        assertThat(entity.getAddressKey()).isEqualTo("web:session:session|scope:checkout");
        assertThat(entity.getScope()).isEqualTo("checkout");
    }

    @Test
    void shouldFindByScopedKey() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        ConversationScope scope = ConversationScope.of("checkout");

        store.findByAddress(address, scope);

        verify(repository).findByAddressKey("web:session:session|scope:checkout");
    }

    @Test
    void shouldDeleteByScopedKey() {
        ConversationAddress address = new ConversationAddress("web", "user", "session");
        ConversationScope scope = ConversationScope.of("checkout");

        store.deleteByAddress(address, scope);

        verify(repository).deleteByAddressKey("web:session:session|scope:checkout");
    }

    private PendingInteraction pendingInteraction(ConversationAddress address) {
        return new PendingInteraction(address, "type", "topic", Question.text("question"), new Facts());
    }
}
