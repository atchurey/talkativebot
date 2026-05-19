package com.atchurey.tools.talkativebot.core.channel;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncomingMessageTest {

    @Test
    void shouldCreateMessageWithDefaultIdAndTimestamp() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        IncomingMessage message = new IncomingMessage(address, "hello");

        assertThat(message.getId()).isNotBlank();
        assertThat(message.getAddress()).isEqualTo(address);
        assertThat(message.getText()).isEqualTo("hello");
        assertThat(message.getReceivedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldCreateMessageWithExplicitDetails() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Instant now = Instant.now();
        IncomingMessage message = new IncomingMessage("id123", address, "hello", now);

        assertThat(message.getId()).isEqualTo("id123");
        assertThat(message.getAddress()).isEqualTo(address);
        assertThat(message.getText()).isEqualTo("hello");
        assertThat(message.getReceivedAt()).isEqualTo(now);
    }

    @Test
    void shouldRequireFieldsInConstructor() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Instant now = Instant.now();

        assertThatThrownBy(() -> new IncomingMessage(null, address, "text", now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new IncomingMessage("id", null, "text", now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new IncomingMessage("id", address, null, now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new IncomingMessage("id", address, "text", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldProduceJsonStringInToString() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        IncomingMessage message = new IncomingMessage("id123", address, "hello", Instant.parse("2023-01-01T00:00:00Z"));

        String toString = message.toString();
        assertThat(toString).contains("\"id\":\"id123\"");
        assertThat(toString).contains("\"text\":\"hello\"");
        assertThat(toString).contains("\"receivedAt\":\"2023-01-01T00:00:00Z\"");
    }
}
