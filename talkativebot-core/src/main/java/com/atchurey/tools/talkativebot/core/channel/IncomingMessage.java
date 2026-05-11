package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class IncomingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final ConversationAddress address;
    private final String text;
    private final Instant receivedAt;

    public IncomingMessage(
            ConversationAddress address,
            String text
    ) {
        this(
                UUID.randomUUID().toString(),
                address,
                text,
                Instant.now()
        );
    }

    @JsonCreator
    public IncomingMessage(
            @JsonProperty("id") String id,
            @JsonProperty("address") ConversationAddress address,
            @JsonProperty("text") String text,
            @JsonProperty("receivedAt") Instant receivedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt must not be null");
    }

}
