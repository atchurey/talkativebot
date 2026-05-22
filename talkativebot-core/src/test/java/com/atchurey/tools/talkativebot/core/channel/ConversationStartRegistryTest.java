package com.atchurey.tools.talkativebot.core.channel;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConversationStartRegistryTest {

    @Test
    void shouldResolveUsingFirstMatchingResolver() {
        IncomingMessage message = mock(IncomingMessage.class);
        ConversationStartRequest request = mock(ConversationStartRequest.class);

        ConversationStartResolver resolver1 = mock(ConversationStartResolver.class);
        when(resolver1.resolve(message)).thenReturn(Optional.empty());

        ConversationStartResolver resolver2 = mock(ConversationStartResolver.class);
        when(resolver2.resolve(message)).thenReturn(Optional.of(request));

        ConversationStartResolver resolver3 = mock(ConversationStartResolver.class);

        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(resolver1, resolver2, resolver3));

        Optional<ConversationStartRequest> result = registry.resolve(message);

        assertThat(result).isPresent().contains(request);
        verify(resolver1).resolve(message);
        verify(resolver2).resolve(message);
        verify(resolver3, never()).resolve(any());
    }

    @Test
    void shouldReturnEmptyIfNoResolverMatches() {
        IncomingMessage message = mock(IncomingMessage.class);

        ConversationStartResolver resolver = mock(ConversationStartResolver.class);
        when(resolver.resolve(message)).thenReturn(Optional.empty());

        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(resolver));

        Optional<ConversationStartRequest> result = registry.resolve(message);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSortResolversByOrder() {
        IncomingMessage message = mock(IncomingMessage.class);
        ConversationStartRequest request1 = mock(ConversationStartRequest.class);
        ConversationStartRequest request2 = mock(ConversationStartRequest.class);

        OrderedConversationStartResolver resolverHighOrder = mock(OrderedConversationStartResolver.class);
        when(resolverHighOrder.getOrder()).thenReturn(100);
        when(resolverHighOrder.resolve(message)).thenReturn(Optional.of(request1));

        OrderedConversationStartResolver resolverLowOrder = mock(OrderedConversationStartResolver.class);
        when(resolverLowOrder.getOrder()).thenReturn(10);
        when(resolverLowOrder.resolve(message)).thenReturn(Optional.of(request2));

        // Low order should come first
        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(resolverHighOrder, resolverLowOrder));

        Optional<ConversationStartRequest> result = registry.resolve(message);

        assertThat(result).isPresent().contains(request2);
        verify(resolverLowOrder).resolve(message);
        verify(resolverHighOrder, never()).resolve(any());
    }

    @Test
    void shouldHandleNullResolvers() {
        ConversationStartRegistry registry = new ConversationStartRegistry(null);
        assertThat(registry.resolve(mock(IncomingMessage.class))).isEmpty();
    }
}
