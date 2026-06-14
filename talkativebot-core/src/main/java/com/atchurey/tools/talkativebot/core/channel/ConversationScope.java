package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * Names the internal workflow lane that should handle a message for a given
 * {@link ConversationAddress}.
 * <p>
 * A scope is not a transport address and should not contain provider-specific
 * channel, user, session, or message ids. It is a generic TalkativeBot routing
 * value such as {@code default}, {@code checkout}, or {@code order-status}.
 */
public record ConversationScope(
        /**
         * Stable generic scope name.
         */
        @JsonProperty("value") String value
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Scope name used by simple bots that only run one workflow per address.
     */
    public static final String DEFAULT_VALUE = "default";

    /**
     * Default scope used when no explicit workflow lane is selected.
     */
    public static final ConversationScope DEFAULT = new ConversationScope(DEFAULT_VALUE);

    /**
     * Creates a scope with a trimmed, non-blank value.
     */
    @JsonCreator
    public ConversationScope {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    /**
     * Creates a scope from a generic workflow lane name.
     */
    public static ConversationScope of(String value) {
        return new ConversationScope(value);
    }
}
