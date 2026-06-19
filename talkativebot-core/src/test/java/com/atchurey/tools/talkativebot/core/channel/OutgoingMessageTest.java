package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class OutgoingMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

    @Test
    void shouldCreateMessageWithEnrichedDetails() {
        ConversationAddress address = new ConversationAddress("stream", "user", "session", "conversation");
        Instant now = Instant.parse("2023-01-01T00:00:00Z");

        ChannelInfo channel = new ChannelInfo("provider", "messaging", "adapter", "conversation", "session", "message");
        ExternalIdentity externalIdentity = new ExternalIdentity("provider", "external-user", "Ada", null, null);
        ReferralContext referral = new ReferralContext(
                "source",
                "source-id",
                "https://example.com/source",
                "catalog-item",
                "target-id",
                "campaign-id",
                Map.of("application_hint", "value")
        );

        OutgoingMessage message = new OutgoingMessage(
                "id123",
                address,
                null,
                "hello",
                now,
                "message.sent",
                channel,
                externalIdentity,
                referral,
                Map.of("adapter_hint", "value"),
                "raw:123"
        );

        assertThat(message.getEventType()).isEqualTo("message.sent");
        assertThat(message.getChannel().getProvider()).isEqualTo("provider");
        assertThat(message.getExternalIdentity().getExternalUserId()).isEqualTo("external-user");
        assertThat(message.getReferral().getTargetType()).isEqualTo("catalog-item");
        assertThat(message.getMetadata()).containsEntry("adapter_hint", "value");
        assertThat(message.getRawPayloadReference()).isEqualTo("raw:123");
    }

    @Test
    void shouldSerializeAndDeserializeEnrichedMessage() throws Exception {
        ConversationAddress address = new ConversationAddress("stream", "user", "session", "conversation");
        Map<String, Serializable> messageMetadata = Map.of("adapter_hint", "value");

        OutgoingMessage original = new OutgoingMessage(
                "id123",
                address,
                null,
                "hello",
                Instant.parse("2023-01-01T00:00:00Z"),
                "message.sent",
                new ChannelInfo("provider", "messaging", "adapter", "conversation", "session", "message"),
                new ExternalIdentity("provider", "external-user", "Ada", null, null),
                new ReferralContext("source", "source-id", "https://example.com/source", "catalog-item", "target-id", "campaign-id", Map.of()),
                messageMetadata,
                "raw:123"
        );

        String json = objectMapper.writeValueAsString(original);
        OutgoingMessage restored = objectMapper.readValue(json, OutgoingMessage.class);

        assertThat(restored.getId()).isEqualTo("id123");
        assertThat(restored.getText()).isEqualTo("hello");
        assertThat(restored.getEventType()).isEqualTo("message.sent");
        assertThat(restored.getChannel().getMessageId()).isEqualTo("message");
        assertThat(restored.getExternalIdentity().getDisplayName()).isEqualTo("Ada");
        assertThat(restored.getMetadata()).containsEntry("adapter_hint", "value");
        assertThat(restored.getRawPayloadReference()).isEqualTo("raw:123");
    }

    @Test
    void shouldCreateFromIncomingMessage() {
        ConversationAddress address = new ConversationAddress("stream", "user", "session", "conversation");
        ChannelInfo channel = new ChannelInfo("p", "m", "a", "c", "s", "msg");
        IncomingMessage incoming = new IncomingMessage(
                "id123", address, "hello", Instant.now(), "event", channel, null, null, Map.of("k", "v"), "raw"
        );

        OutgoingMessage outgoing = OutgoingMessage.from(incoming)
                .text("reply")
                .eventType("reply.event")
                .build();

        assertThat(outgoing.getId()).isNotEqualTo(incoming.getId());
        assertThat(outgoing.getAddress()).isEqualTo(address);
        assertThat(outgoing.getText()).isEqualTo("reply");
        assertThat(outgoing.getEventType()).isEqualTo("reply.event");
        assertThat(outgoing.getChannel()).isEqualTo(channel);
        assertThat(outgoing.getMetadata()).containsEntry("k", "v");
        assertThat(outgoing.getRawPayloadReference()).isEqualTo("raw");
    }

    @Test
    void shouldSupportToBuilder() {
        ConversationAddress address = new ConversationAddress("web", "user", null, null);
        OutgoingMessage original = OutgoingMessage.text(address, "hi");

        OutgoingMessage enriched = original.toBuilder()
                .eventType("enriched")
                .metadata(Map.of("key", "value"))
                .build();

        assertThat(enriched.getId()).isEqualTo(original.getId());
        assertThat(enriched.getText()).isEqualTo("hi");
        assertThat(enriched.getEventType()).isEqualTo("enriched");
        assertThat(enriched.getMetadata()).containsEntry("key", "value");
    }
}
