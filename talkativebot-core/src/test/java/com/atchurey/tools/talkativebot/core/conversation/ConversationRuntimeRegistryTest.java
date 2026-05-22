package com.atchurey.tools.talkativebot.core.conversation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationRuntimeRegistryTest {

    private ConversationRuntimeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ConversationRuntimeRegistry();
    }

    @Test
    void shouldGetOrInitializeRuntime() {
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        ConversationRuntime runtime = mock(ConversationRuntime.class);
        when(initializer.initialize(any())).thenReturn(runtime);

        Class<? extends Conversation<?>> type = TestConversation.class;
        
        ConversationRuntime result = registry.getOrInitialize(type, initializer);
        
        assertThat(result).isSameAs(runtime);
        verify(initializer, times(1)).initialize(type);
        assertThat(registry.contains(type)).isTrue();
    }

    @Test
    void shouldNotInitializeTwice() {
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        ConversationRuntime runtime = mock(ConversationRuntime.class);
        when(initializer.initialize(any())).thenReturn(runtime);

        Class<? extends Conversation<?>> type = TestConversation.class;
        
        registry.getOrInitialize(type, initializer);
        ConversationRuntime result = registry.getOrInitialize(type, initializer);
        
        assertThat(result).isSameAs(runtime);
        verify(initializer, times(1)).initialize(type);
    }

    @Test
    void shouldThrowExceptionWhenArgumentsAreNull() {
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        Class<? extends Conversation<?>> type = TestConversation.class;

        assertThatThrownBy(() -> registry.getOrInitialize(null, initializer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("conversationType must not be null");

        assertThatThrownBy(() -> registry.getOrInitialize(type, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("initializer must not be null");
    }

    @Test
    void shouldCheckIfContains() {
        assertThat(registry.contains(TestConversation.class)).isFalse();
        assertThat(registry.contains(null)).isFalse();
        
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        when(initializer.initialize(any())).thenReturn(mock(ConversationRuntime.class));
        
        registry.getOrInitialize(TestConversation.class, initializer);
        assertThat(registry.contains(TestConversation.class)).isTrue();
    }

    @Test
    void shouldClearRuntimes() {
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        when(initializer.initialize(any())).thenReturn(mock(ConversationRuntime.class));
        
        registry.getOrInitialize(TestConversation.class, initializer);
        assertThat(registry.contains(TestConversation.class)).isTrue();
        
        registry.clear();
        assertThat(registry.contains(TestConversation.class)).isFalse();
    }

    @Test
    void shouldCloseRuntimesOnClose() throws Exception {
        ConversationRuntime runtime1 = mock(ConversationRuntime.class);
        ConversationRuntime runtime2 = mock(ConversationRuntime.class);
        
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        when(initializer.initialize(any()))
                .thenReturn(runtime1)
                .thenReturn(runtime2);
        
        registry.getOrInitialize(TestConversation.class, initializer);
        registry.getOrInitialize(OtherConversation.class, initializer);
        
        registry.close();
        
        verify(runtime1).close();
        verify(runtime2).close();
        assertThat(registry.contains(TestConversation.class)).isFalse();
    }

    @Test
    void shouldIgnoreExceptionsDuringClose() throws Exception {
        ConversationRuntime runtime = mock(ConversationRuntime.class);
        doThrow(new RuntimeException("failed")).when(runtime).close();
        
        ConversationRuntimeInitializer initializer = mock(ConversationRuntimeInitializer.class);
        when(initializer.initialize(any())).thenReturn(runtime);
        
        registry.getOrInitialize(TestConversation.class, initializer);
        
        registry.close(); // Should not throw exception
        
        verify(runtime).close();
    }

    private interface TestConversation extends Conversation<String> {}
    private interface OtherConversation extends Conversation<String> {}
}
