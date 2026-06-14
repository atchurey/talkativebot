package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationScopeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldExposeDefaultScope() {
        assertThat(ConversationScope.DEFAULT.value()).isEqualTo("default");
    }

    @Test
    void shouldCreateScopeFromValue() {
        ConversationScope scope = ConversationScope.of("checkout");

        assertThat(scope.value()).isEqualTo("checkout");
    }

    @Test
    void shouldTrimScopeValue() {
        ConversationScope scope = ConversationScope.of("  order-status  ");

        assertThat(scope.value()).isEqualTo("order-status");
    }

    @Test
    void shouldRequireValue() {
        assertThatThrownBy(() -> ConversationScope.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value must not be null");
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> ConversationScope.of(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value must not be blank");
    }

    @Test
    void shouldSerializeAndDeserializeJson() throws Exception {
        ConversationScope original = ConversationScope.of("payment");

        String json = objectMapper.writeValueAsString(original);
        ConversationScope restored = objectMapper.readValue(json, ConversationScope.class);

        assertThat(json).isEqualTo("{\"value\":\"payment\"}");
        assertThat(restored).isEqualTo(original);
    }
}
