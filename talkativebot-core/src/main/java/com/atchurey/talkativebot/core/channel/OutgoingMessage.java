package com.atchurey.talkativebot.core.channel;

import com.atchurey.talkativebot.core.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class OutgoingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final ConversationAddress address;
    private final Question question;
    private final String text;
    private final Instant createdAt;

    @JsonCreator
    public OutgoingMessage(
            @JsonProperty("id") String id,
            @JsonProperty("address") ConversationAddress address,
            @JsonProperty("question") Question question,
            @JsonProperty("text") String text,
            @JsonProperty("createdAt") Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.question = question;
        this.text = text;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (this.question == null && this.text == null) {
            throw new IllegalArgumentException("Either question or text must be provided");
        }
    }

    public static OutgoingMessage question(ConversationAddress address, Question question) {
        return new OutgoingMessage(
                UUID.randomUUID().toString(),
                address,
                Objects.requireNonNull(question, "question must not be null"),
                null,
                Instant.now()
        );
    }

    public static OutgoingMessage text(ConversationAddress address, String text) {
        return new OutgoingMessage(
                UUID.randomUUID().toString(),
                address,
                null,
                Objects.requireNonNull(text, "text must not be null"),
                Instant.now()
        );
    }

    public boolean isQuestion() {
        return question != null;
    }
}
