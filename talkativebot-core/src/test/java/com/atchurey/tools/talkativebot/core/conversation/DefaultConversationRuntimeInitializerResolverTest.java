package com.atchurey.tools.talkativebot.core.conversation;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultConversationRuntimeInitializerResolverTest {

    @Test
    void shouldResolveToDefaultWhenNoOthersMatch() {
        DefaultConversationRuntimeInitializerResolver resolver = DefaultConversationRuntimeInitializerResolver.INSTANCE;
        ConversationRuntimeInitializer result = resolver.resolve(TestConversation.class);
        
        assertThat(result).isSameAs(DefaultConversationRuntimeInitializer.INSTANCE);
    }

    @Test
    void shouldResolveToMatchingInitializer() {
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        when(initializer.supports(any())).thenReturn(true);
        
        DefaultConversationRuntimeInitializerResolver resolver = 
                new DefaultConversationRuntimeInitializerResolver(List.of(initializer));
        
        ConversationRuntimeInitializer result = resolver.resolve(TestConversation.class);
        
        assertThat(result).isSameAs(initializer);
    }

    @Test
    void shouldHandleNullInitializers() {
        DefaultConversationRuntimeInitializerResolver resolver = new DefaultConversationRuntimeInitializerResolver(null);
        ConversationRuntimeInitializer result = resolver.resolve(TestConversation.class);
        
        assertThat(result).isSameAs(DefaultConversationRuntimeInitializer.INSTANCE);
    }

    @Test
    void shouldDefaultInitializerAlwaysSupport() {
        DefaultConversationRuntimeInitializer initializer = DefaultConversationRuntimeInitializer.INSTANCE;
        assertThat(initializer.supports(TestConversation.class)).isTrue();
    }

    @Test
    void shouldDefaultInitializerInitializeToEmptyRuntime() {
        DefaultConversationRuntimeInitializer initializer = DefaultConversationRuntimeInitializer.INSTANCE;
        ConversationRuntime runtime = initializer.initialize(TestConversation.class);
        
        assertThat(runtime).isSameAs(EmptyConversationRuntime.INSTANCE);
    }

    private interface TestConversation extends Conversation<String> {}
}
