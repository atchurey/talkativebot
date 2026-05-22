package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Question;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PendingInteractionTest {

    @Test
    void shouldCreatePendingInteraction() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Question question = mock(Question.class);
        Facts facts = new Facts();
        PendingInteraction interaction = new PendingInteraction(address, "type", "topic", question, facts);

        assertThat(interaction.getId()).isNotBlank();
        assertThat(interaction.getAddress()).isEqualTo(address);
        assertThat(interaction.getConversationType()).isEqualTo("type");
        assertThat(interaction.getCurrentTopicKey()).isEqualTo("topic");
        assertThat(interaction.getQuestion()).isEqualTo(question);
        assertThat(interaction.getFacts()).isEqualTo(facts);
        assertThat(interaction.getCreatedAt()).isNotNull();
        assertThat(interaction.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreateWithExplicitDetails() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Question question = mock(Question.class);
        Facts facts = new Facts();
        Instant now = Instant.now();
        PendingInteraction interaction = new PendingInteraction("id123", address, "type", "topic", question, facts, now, now);

        assertThat(interaction.getId()).isEqualTo("id123");
        assertThat(interaction.getCreatedAt()).isEqualTo(now);
        assertThat(interaction.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRequireFields() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Question question = mock(Question.class);
        Facts facts = new Facts();
        Instant now = Instant.now();

        assertThatThrownBy(() -> new PendingInteraction(null, address, "type", "topic", question, facts, now, now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PendingInteraction("id", null, "type", "topic", question, facts, now, now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PendingInteraction("id", address, null, "topic", question, facts, now, now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PendingInteraction("id", address, "type", "topic", null, facts, now, now)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldInitializeFactsIfNull() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        Question question = mock(Question.class);
        PendingInteraction interaction = new PendingInteraction(address, "type", "topic", question, null);

        assertThat(interaction.getFacts()).isNotNull();
    }
}
