package com.atchurey.tools.talkativebot.core.store;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
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

    @Test
    void shouldKeepDefaultScopeCompatibleWithAddressKey() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", null);
        PendingInteraction interaction = mock(PendingInteraction.class);
        when(interaction.getAddress()).thenReturn(address);

        store.save(interaction, ConversationScope.DEFAULT);

        Optional<PendingInteraction> result = store.findByAddress(address);
        assertThat(result).isPresent().contains(interaction);
    }

    @Test
    void shouldStoreMultipleScopesForSameAddress() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", null);
        ConversationScope checkoutScope = ConversationScope.of("checkout");
        ConversationScope statusScope = ConversationScope.of("order-status");
        PendingInteraction checkoutInteraction = mock(PendingInteraction.class);
        PendingInteraction statusInteraction = mock(PendingInteraction.class);
        when(checkoutInteraction.getAddress()).thenReturn(address);
        when(statusInteraction.getAddress()).thenReturn(address);

        store.save(checkoutInteraction, checkoutScope);
        store.save(statusInteraction, statusScope);

        Optional<PendingInteraction> checkoutResult = store.findByAddress(address, checkoutScope);
        Optional<PendingInteraction> statusResult = store.findByAddress(address, statusScope);
        Optional<PendingInteraction> defaultResult = store.findByAddress(address);

        assertThat(checkoutResult).isPresent().contains(checkoutInteraction);
        assertThat(statusResult).isPresent().contains(statusInteraction);
        assertThat(defaultResult).isEmpty();
    }

    @Test
    void shouldDeleteOnlyRequestedScope() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", null);
        ConversationScope checkoutScope = ConversationScope.of("checkout");
        ConversationScope statusScope = ConversationScope.of("order-status");
        PendingInteraction checkoutInteraction = mock(PendingInteraction.class);
        PendingInteraction statusInteraction = mock(PendingInteraction.class);
        when(checkoutInteraction.getAddress()).thenReturn(address);
        when(statusInteraction.getAddress()).thenReturn(address);

        store.save(checkoutInteraction, checkoutScope);
        store.save(statusInteraction, statusScope);
        store.deleteByAddress(address, checkoutScope);

        Optional<PendingInteraction> checkoutResult = store.findByAddress(address, checkoutScope);
        Optional<PendingInteraction> statusResult = store.findByAddress(address, statusScope);

        assertThat(checkoutResult).isEmpty();
        assertThat(statusResult).isPresent().contains(statusInteraction);
    }
}
