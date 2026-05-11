package com.atchurey.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class ConversationAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String channel;
    private final String userId;
    private final String sessionId;
    private final String conversationId;

    @JsonCreator
    public ConversationAddress(
            @JsonProperty("channel") String channel,
            @JsonProperty("userId") String userId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("conversationId") String conversationId
    ) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
        this.userId = userId;
        this.sessionId = sessionId;
        this.conversationId = conversationId;
    }

	public String persistenceKey() {
        if (conversationId != null && !conversationId.isBlank()) {
            return channel + ":conversation:" + conversationId;
        }

        if (sessionId != null && !sessionId.isBlank()) {
            return channel + ":session:" + sessionId;
        }

        if (userId != null && !userId.isBlank()) {
            return channel + ":user:" + userId;
        }

        throw new IllegalStateException("ConversationAddress must contain conversationId, sessionId, or userId");
    }
}