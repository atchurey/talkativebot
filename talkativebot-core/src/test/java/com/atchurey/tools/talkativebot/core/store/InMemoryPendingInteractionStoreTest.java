package com.atchurey.tools.talkativebot.core.store;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryPendingInteractionStoreTest {

    private final InMemoryPendingInteractionStore store = new InMemoryPendingInteractionStore();

    @Test
    void saveAndFind() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        PendingInteraction interaction = mock(PendingInteraction.class);
        when(interaction.getAddress()).thenReturn(address);

        store.save(interaction);

        Optional<PendingInteraction> result = store.findByAddress(address);
        assertThat(result).isPresent().contains(interaction);
    }

    @Test
    void saveWithTtlAndFindBeforeExpiry() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        PendingInteraction interaction = mock(PendingInteraction.class);
        when(interaction.getAddress()).thenReturn(address);

        store.save(interaction, Duration.ofMinutes(1));

        Optional<PendingInteraction> result = store.findByAddress(address);
        assertThat(result).isPresent().contains(interaction);
    }

    @Test
    void saveWithTtlAndFindAfterExpiry() throws InterruptedException {
        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        PendingInteraction interaction = mock(PendingInteraction.class);
        when(interaction.getAddress()).thenReturn(address);

        store.save(interaction, Duration.ofMillis(10));
        Thread.sleep(20);

        Optional<PendingInteraction> result = store.findByAddress(address);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByAddress() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        PendingInteraction interaction = mock(PendingInteraction.class);
        when(interaction.getAddress()).thenReturn(address);

        store.save(interaction);
        store.deleteByAddress(address);

        Optional<PendingInteraction> result = store.findByAddress(address);
        assertThat(result).isEmpty();
    }
}
