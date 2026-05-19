package com.atchurey.tools.talkativebot.core.channel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationAddressTest {

    @Test
    void shouldCreateAddress() {
        ConversationAddress address = new ConversationAddress("web", "user123", "session456", "conv789");

        assertThat(address.getChannel()).isEqualTo("web");
        assertThat(address.getUserId()).isEqualTo("user123");
        assertThat(address.getSessionId()).isEqualTo("session456");
        assertThat(address.getConversationId()).isEqualTo("conv789");
    }

    @Test
    void shouldRequireChannel() {
        assertThatThrownBy(() -> new ConversationAddress(null, "user", "session", "conv"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("channel must not be null");
    }

    @Test
    void shouldReturnPersistenceKeyForConversationId() {
        ConversationAddress address = new ConversationAddress("web", "user", "session", "conv");
        assertThat(address.persistenceKey()).isEqualTo("web:conversation:conv");
    }

    @Test
    void shouldReturnPersistenceKeyForSessionIdIfNoConversationId() {
        ConversationAddress address = new ConversationAddress("web", "user", "session", null);
        assertThat(address.persistenceKey()).isEqualTo("web:session:session");
    }

    @Test
    void shouldReturnPersistenceKeyForUserIdIfNoConversationOrSessionId() {
        ConversationAddress address = new ConversationAddress("web", "user", null, "");
        assertThat(address.persistenceKey()).isEqualTo("web:user:user");
    }

    @Test
    void shouldThrowIfNoIdentifierPresent() {
        ConversationAddress address = new ConversationAddress("web", null, "", " ");
        assertThatThrownBy(address::persistenceKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ConversationAddress must contain conversationId, sessionId, or userId");
    }
}
