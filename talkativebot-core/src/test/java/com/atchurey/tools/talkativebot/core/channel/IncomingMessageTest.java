package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncomingMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
        assertThat(message.getMetadata()).isEmpty();
    }

    @Test
    void shouldCreateMessageWithEnvelopeDetails() {
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

        IncomingMessage message = new IncomingMessage(
                "id123",
                address,
                "hello",
                now,
                "message.referral",
                channel,
                externalIdentity,
                referral,
                Map.of("adapter_hint", "value"),
                "raw:123"
        );

        assertThat(message.getEventType()).isEqualTo("message.referral");
        assertThat(message.getChannel().getProvider()).isEqualTo("provider");
        assertThat(message.getExternalIdentity().getExternalUserId()).isEqualTo("external-user");
        assertThat(message.getReferral().getTargetType()).isEqualTo("catalog-item");
        assertThat(message.getReferral().getTargetId()).isEqualTo("target-id");
        assertThat(message.getReferral().getMetadata()).containsEntry("application_hint", "value");
        assertThat(message.getMetadata()).containsEntry("adapter_hint", "value");
        assertThat(message.getRawPayloadReference()).isEqualTo("raw:123");
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

    @Test
    void shouldDeserializeLegacyJsonMessage() throws Exception {
        String json = """
                {
                  "id": "id123",
                  "address": {
                    "channel": "web",
                    "userId": "user",
                    "sessionId": null,
                    "conversationId": null
                  },
                  "text": "hello",
                  "receivedAt": "2023-01-01T00:00:00Z"
                }
                """;

        IncomingMessage message = objectMapper.readValue(json, IncomingMessage.class);

        assertThat(message.getId()).isEqualTo("id123");
        assertThat(message.getAddress().getChannel()).isEqualTo("web");
        assertThat(message.getText()).isEqualTo("hello");
        assertThat(message.getReceivedAt()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
        assertThat(message.getEventType()).isNull();
        assertThat(message.getMetadata()).isEmpty();
    }

    @Test
    void shouldSerializeAndDeserializeEnvelopeJsonMessage() throws Exception {
        ConversationAddress address = new ConversationAddress("stream", "user", "session", "conversation");
        Map<String, Serializable> messageMetadata = Map.of("adapter_hint", "value");
        Map<String, Serializable> referralMetadata = Map.of("application_hint", "value");

        IncomingMessage original = new IncomingMessage(
                "id123",
                address,
                "",
                Instant.parse("2023-01-01T00:00:00Z"),
                "message.referral",
                new ChannelInfo("provider", "messaging", "adapter", "conversation", "session", "message"),
                new ExternalIdentity("provider", "external-user", "Ada", null, null),
                new ReferralContext("source", "source-id", "https://example.com/source", "catalog-item", "target-id", "campaign-id", referralMetadata),
                messageMetadata,
                "raw:123"
        );

        String json = objectMapper.writeValueAsString(original);
        IncomingMessage restored = objectMapper.readValue(json, IncomingMessage.class);

        assertThat(restored.getId()).isEqualTo("id123");
        assertThat(restored.getText()).isEmpty();
        assertThat(restored.getEventType()).isEqualTo("message.referral");
        assertThat(restored.getChannel().getMessageId()).isEqualTo("message");
        assertThat(restored.getExternalIdentity().getDisplayName()).isEqualTo("Ada");
        assertThat(restored.getReferral().getTargetType()).isEqualTo("catalog-item");
        assertThat(restored.getReferral().getTargetId()).isEqualTo("target-id");
        assertThat(restored.getReferral().getMetadata()).containsEntry("application_hint", "value");
        assertThat(restored.getMetadata()).containsEntry("adapter_hint", "value");
        assertThat(restored.getRawPayloadReference()).isEqualTo("raw:123");
    }
}
