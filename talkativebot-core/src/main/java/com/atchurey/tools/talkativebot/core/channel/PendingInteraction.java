package com.atchurey.talkativebot.core.channel;

import com.atchurey.talkativebot.core.conversation.Facts;
import com.atchurey.talkativebot.core.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PendingInteraction implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final ConversationAddress address;
    private final String conversationType;
    private final String currentTopicKey;
    private final Question question;
    private final Facts facts;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PendingInteraction(
            ConversationAddress address,
            String conversationType,
            String currentTopicKey,
            Question question,
            Facts facts
    ) {
        this(
                UUID.randomUUID().toString(),
                address,
                conversationType,
                currentTopicKey,
                question,
                facts,
                Instant.now(),
                Instant.now()
        );
    }

    @JsonCreator
    public PendingInteraction(
            @JsonProperty("id") String id,
            @JsonProperty("address") ConversationAddress address,
            @JsonProperty("conversationType") String conversationType,
            @JsonProperty("currentTopicKey") String currentTopicKey,
            @JsonProperty("question") Question question,
            @JsonProperty("facts") Facts facts,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.conversationType = Objects.requireNonNull(conversationType, "conversationType must not be null");
        this.currentTopicKey = currentTopicKey;
        this.question = Objects.requireNonNull(question, "question must not be null");
        this.facts = facts == null ? new Facts() : facts;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

}