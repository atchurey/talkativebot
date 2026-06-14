package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identifies where a message came from and where replies should be delivered.
 * <p>
 * The {@code channel} value must match a TalkativeBot input/output channel that
 * the application supports. {@code userId} and {@code sessionId} identify the
 * external user/session on that channel. Internal workflow routing belongs in
 * {@link ConversationScope}, not in this address.
 */
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

    /**
     * Creates a non-legacy address using the supported TalkativeBot channel and
     * the external user/session identifiers available to the integration.
     */
    public ConversationAddress(
            String channel,
            String userId,
            String sessionId
    ) {
        this(channel, userId, sessionId, null);
    }

    /**
     * Creates an address, including the legacy {@code conversationId} field for
     * JSON compatibility with existing clients and persisted interactions.
     *
     * @deprecated use {@link #ConversationAddress(String, String, String)} and
     * keep external platform conversation/thread ids in {@link ChannelInfo}.
     */
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

    /**
     * Returns the current pending-interaction key for this external address.
     * <p>
     * The legacy {@code conversationId} branch is still checked first so old
     * persisted interactions can resume after an upgrade. New scoped routing will
     * compose this address identity with {@link ConversationScope} instead of
     * overloading the address itself.
     */
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
