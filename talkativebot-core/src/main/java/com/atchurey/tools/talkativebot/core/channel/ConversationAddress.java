package com.atchurey.tools.talkativebot.core.channel;

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

    /**
     * Legacy external conversation/thread identifier. New integrations should keep
     * platform-specific conversation ids in channel metadata and use sessionId or userId
     * for TalkativeBot pending-interaction identity.
     */
    @Deprecated(since = "0.0.1", forRemoval = false)
    private final String conversationId;

    public ConversationAddress(
            String channel,
            String userId,
            String sessionId
    ) {
        this(channel, userId, sessionId, null);
    }

    @Deprecated(since = "0.0.1", forRemoval = false)
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
        // Keep the legacy conversationId branch temporarily so existing persisted
        // pending interactions can still resume after applications upgrade.
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
