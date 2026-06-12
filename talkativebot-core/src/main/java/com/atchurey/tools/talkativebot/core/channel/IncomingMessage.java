package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public class IncomingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final String id;
    private final ConversationAddress address;
    private final String text;
    private final Instant receivedAt;
    private final String eventType;
    private final ChannelInfo channel;
    private final ExternalIdentity externalIdentity;
    private final ReferralContext referral;
    private final Map<String, Serializable> metadata;
    private final String rawPayloadReference;

    public IncomingMessage(
            ConversationAddress address,
            String text) {
        this(
                UUID.randomUUID().toString(),
                address,
                text,
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public IncomingMessage(
            String id,
            ConversationAddress address,
            String text,
            Instant receivedAt) {
        this(
                id,
                address,
                text,
                receivedAt,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @JsonCreator
    public IncomingMessage(
            @JsonProperty("id") String id,
            @JsonProperty("address") ConversationAddress address,
            @JsonProperty("text") String text,
            @JsonProperty("receivedAt") Instant receivedAt,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("channel") ChannelInfo channel,
            @JsonProperty("externalIdentity") ExternalIdentity externalIdentity,
            @JsonProperty("referral") ReferralContext referral,
            @JsonProperty("metadata") Map<String, Serializable> metadata,
            @JsonProperty("rawPayloadReference") String rawPayloadReference
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt must not be null");
        this.eventType = eventType;
        this.channel = channel;
        this.externalIdentity = externalIdentity;
        this.referral = referral;
        this.metadata = metadata == null ? Collections.emptyMap() : Map.copyOf(metadata);
        this.rawPayloadReference = rawPayloadReference;
    }

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "IncomingMessage{" +
                    "id='" + id + '\'' +
                    ", address=" + address +
                    ", text='" + text + '\'' +
                    ", receivedAt=" + receivedAt +
                    '}';
        }
    }

}
