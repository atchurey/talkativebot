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
    void shouldUseUnscopedResolverForDefaultScope() {
        IncomingMessage message = mock(IncomingMessage.class);
        ConversationStartRequest request = mock(ConversationStartRequest.class);

        ConversationStartResolver resolver = mock(ConversationStartResolver.class);
        when(resolver.resolve(message)).thenReturn(Optional.of(request));

        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(resolver));

        Optional<ConversationStartRequest> result = registry.resolve(message, ConversationScope.DEFAULT);

        assertThat(result).isPresent().contains(request);
        verify(resolver).resolve(message);
    }

    @Test
    void shouldSkipUnscopedResolverForNonDefaultScope() {
        IncomingMessage message = mock(IncomingMessage.class);

        ConversationStartResolver resolver = mock(ConversationStartResolver.class);

        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(resolver));

        Optional<ConversationStartRequest> result = registry.resolve(message, ConversationScope.of("status"));

        assertThat(result).isEmpty();
        verify(resolver, never()).resolve(any());
    }

    @Test
    void shouldInvokeScopedResolverOnlyWhenScopeIsSupported() {
        IncomingMessage message = mock(IncomingMessage.class);
        ConversationStartRequest request = mock(ConversationStartRequest.class);
        ConversationScope checkoutScope = ConversationScope.of("checkout");
        ConversationScope statusScope = ConversationScope.of("status");

        ScopedConversationStartResolver checkoutResolver = mock(ScopedConversationStartResolver.class);
        when(checkoutResolver.supportsScope(statusScope)).thenReturn(false);

        ScopedConversationStartResolver statusResolver = mock(ScopedConversationStartResolver.class);
        when(statusResolver.supportsScope(statusScope)).thenReturn(true);
        when(statusResolver.resolve(message)).thenReturn(Optional.of(request));

        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(checkoutResolver, statusResolver));

        Optional<ConversationStartRequest> result = registry.resolve(message, statusScope);

        assertThat(result).isPresent().contains(request);
        verify(checkoutResolver).supportsScope(statusScope);
        verify(checkoutResolver, never()).resolve(any());
        verify(statusResolver).supportsScope(statusScope);
        verify(statusResolver).resolve(message);
        verify(checkoutResolver, never()).supportsScope(checkoutScope);
    }

    @Test
    void shouldPreserveResolverOrderingAfterScopeFiltering() {
        IncomingMessage message = mock(IncomingMessage.class);
        ConversationStartRequest request1 = mock(ConversationStartRequest.class);
        ConversationStartRequest request2 = mock(ConversationStartRequest.class);
        ConversationScope scope = ConversationScope.of("status");

        OrderedScopedConversationStartResolver highOrderResolver = mock(OrderedScopedConversationStartResolver.class);
        when(highOrderResolver.getOrder()).thenReturn(100);
        when(highOrderResolver.supportsScope(scope)).thenReturn(true);
        when(highOrderResolver.resolve(message)).thenReturn(Optional.of(request1));

        OrderedScopedConversationStartResolver lowOrderResolver = mock(OrderedScopedConversationStartResolver.class);
        when(lowOrderResolver.getOrder()).thenReturn(10);
        when(lowOrderResolver.supportsScope(scope)).thenReturn(true);
        when(lowOrderResolver.resolve(message)).thenReturn(Optional.of(request2));

        ConversationStartRegistry registry = new ConversationStartRegistry(List.of(highOrderResolver, lowOrderResolver));

        Optional<ConversationStartRequest> result = registry.resolve(message, scope);

        assertThat(result).isPresent().contains(request2);
        verify(lowOrderResolver).resolve(message);
        verify(highOrderResolver, never()).resolve(any());
    }

    @Test
    void shouldHandleNullResolvers() {
        ConversationStartRegistry registry = new ConversationStartRegistry(null);
        assertThat(registry.resolve(mock(IncomingMessage.class))).isEmpty();
    }

    interface OrderedScopedConversationStartResolver extends ScopedConversationStartResolver, OrderedConversationStartResolver {
    }
}
