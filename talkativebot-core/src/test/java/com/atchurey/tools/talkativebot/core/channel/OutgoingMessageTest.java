package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Question;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class OutgoingMessageTest {

    @Test
    void shouldCreateTextMessage() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        OutgoingMessage message = OutgoingMessage.text(address, "hi");

        assertThat(message.getId()).isNotBlank();
        assertThat(message.getAddress()).isEqualTo(address);
        assertThat(message.getText()).isEqualTo("hi");
        assertThat(message.getQuestion()).isNull();
        assertThat(message.isQuestion()).isFalse();
        assertThat(message.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldCreateQuestionMessage() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Question question = mock(Question.class);
        OutgoingMessage message = OutgoingMessage.question(address, question);

        assertThat(message.getId()).isNotBlank();
        assertThat(message.getAddress()).isEqualTo(address);
        assertThat(message.getText()).isNull();
        assertThat(message.getQuestion()).isEqualTo(question);
        assertThat(message.isQuestion()).isTrue();
    }

    @Test
    void shouldRequireEitherTextOrQuestion() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        assertThatThrownBy(() -> new OutgoingMessage("id", address, null, null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either question or text must be provided");
    }

    @Test
    void shouldRequireFieldsInConstructor() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Instant now = Instant.now();

        assertThatThrownBy(() -> new OutgoingMessage(null, address, null, "text", now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new OutgoingMessage("id", null, null, "text", now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new OutgoingMessage("id", address, null, "text", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldProduceJsonStringInToString() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        OutgoingMessage message = new OutgoingMessage("id123", address, null, "hello", Instant.parse("2023-01-01T00:00:00Z"));

        String toString = message.toString();
        assertThat(toString).contains("\"id\":\"id123\"");
        assertThat(toString).contains("\"text\":\"hello\"");
        assertThat(toString).contains("\"createdAt\":\"2023-01-01T00:00:00Z\"");
    }
}
