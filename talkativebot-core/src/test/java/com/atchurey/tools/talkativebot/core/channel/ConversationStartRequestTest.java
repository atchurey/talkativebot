package com.atchurey.tools.talkativebot.core.channel;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationStartRequestTest {

    @Test
    void shouldCreateRequest() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Map<String, Serializable> facts = Map.of("key", "value");
        ConversationStartRequest request = new ConversationStartRequest(address, "type", "trigger", facts);

        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getConversationType()).isEqualTo("type");
        assertThat(request.getTrigger()).isEqualTo("trigger");
        assertThat(request.getInitialFacts()).containsEntry("key", "value");
    }

    @Test
    void shouldHandleNullFacts() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        ConversationStartRequest request = new ConversationStartRequest(address, "type", "trigger", null);

        assertThat(request.getInitialFacts()).isNotNull().isEmpty();
    }

    @Test
    void shouldRequireAddress() {
        assertThatThrownBy(() -> new ConversationStartRequest(null, "type", "trigger", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("address must not be null");
    }

    @Test
    void shouldRequireType() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        assertThatThrownBy(() -> new ConversationStartRequest(address, null, "trigger", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("conversationType must not be null");
    }
}
