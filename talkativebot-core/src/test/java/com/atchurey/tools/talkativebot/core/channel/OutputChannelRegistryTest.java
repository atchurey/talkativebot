package com.atchurey.tools.talkativebot.core.channel;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OutputChannelRegistryTest {

    @Test
    void shouldSendViaSupportedChannel() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        OutgoingMessage message = mock(OutgoingMessage.class);
        when(message.getAddress()).thenReturn(address);

        OutputChannel channel1 = mock(OutputChannel.class);
        when(channel1.supports(address)).thenReturn(false);

        OutputChannel channel2 = mock(OutputChannel.class);
        when(channel2.supports(address)).thenReturn(true);
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(channel2.send(message)).thenReturn(future);

        OutputChannelRegistry registry = new OutputChannelRegistry(List.of(channel1, channel2));

        CompletableFuture<Void> result = registry.send(message);

        assertThat(result).isSameAs(future);
        verify(channel1).supports(address);
        verify(channel2).supports(address);
        verify(channel2).send(message);
        verify(channel1, never()).send(any());
    }

    @Test
    void shouldThrowIfNoChannelSupportsAddress() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        OutgoingMessage message = mock(OutgoingMessage.class);
        when(message.getAddress()).thenReturn(address);

        OutputChannel channel = mock(OutputChannel.class);
        when(channel.supports(address)).thenReturn(false);

        OutputChannelRegistry registry = new OutputChannelRegistry(List.of(channel));

        assertThatThrownBy(() -> registry.send(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No output channel supports address channel: web");
    }

    @Test
    void shouldRequireNonNullChannels() {
        assertThatThrownBy(() -> new OutputChannelRegistry(null))
                .isInstanceOf(NullPointerException.class);
    }
}
