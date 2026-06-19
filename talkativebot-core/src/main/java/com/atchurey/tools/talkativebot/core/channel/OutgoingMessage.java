package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public class OutgoingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final String id;
    private final ConversationAddress address;
    private final Question question;
    private final String text;
    private final Instant createdAt;
    private final String eventType;
    private final ChannelInfo channel;
    private final ExternalIdentity externalIdentity;
    private final ReferralContext referral;
    private final Map<String, Serializable> metadata;
    private final String rawPayloadReference;

    @JsonCreator
    @Builder(toBuilder = true)
    public OutgoingMessage(
            @JsonProperty("id") String id,
            @JsonProperty("address") ConversationAddress address,
            @JsonProperty("question") Question question,
            @JsonProperty("text") String text,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("channel") ChannelInfo channel,
            @JsonProperty("externalIdentity") ExternalIdentity externalIdentity,
            @JsonProperty("referral") ReferralContext referral,
            @JsonProperty("metadata") Map<String, Serializable> metadata,
            @JsonProperty("rawPayloadReference") String rawPayloadReference
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.question = question;
        this.text = text;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.eventType = eventType;
        this.channel = channel;
        this.externalIdentity = externalIdentity;
        this.referral = referral;
        this.metadata = metadata == null ? Collections.emptyMap() : Map.copyOf(metadata);
        this.rawPayloadReference = rawPayloadReference;

        if (this.question == null && this.text == null) {
            throw new IllegalArgumentException("Either question or text must be provided");
        }
    }

    public OutgoingMessage(
            String id,
            ConversationAddress address,
            Question question,
            String text,
            Instant createdAt
    ) {
        this(
                id,
                address,
                question,
                text,
                createdAt,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static OutgoingMessage question(ConversationAddress address, Question question) {
        return new OutgoingMessage(
                UUID.randomUUID().toString(),
                address,
                Objects.requireNonNull(question, "question must not be null"),
                null,
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static OutgoingMessage text(ConversationAddress address, String text) {
        return new OutgoingMessage(
                UUID.randomUUID().toString(),
                address,
                null,
                Objects.requireNonNull(text, "text must not be null"),
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static OutgoingMessageBuilder from(IncomingMessage incoming) {
        return OutgoingMessage.builder()
                .id(UUID.randomUUID().toString())
                .address(incoming.getAddress())
                .createdAt(Instant.now())
                .channel(incoming.getChannel())
                .externalIdentity(incoming.getExternalIdentity())
                .referral(incoming.getReferral())
                .metadata(incoming.getMetadata())
                .rawPayloadReference(incoming.getRawPayloadReference());
    }

    @JsonIgnore
    public boolean isQuestion() {
        return question != null;
    }

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "OutgoingMessage{" +
                    "id='" + id + '\'' +
                    ", address=" + address +
                    ", question=" + question +
                    ", text='" + text + '\'' +
                    ", createdAt=" + createdAt +
                    '}';
        }
    }
}
