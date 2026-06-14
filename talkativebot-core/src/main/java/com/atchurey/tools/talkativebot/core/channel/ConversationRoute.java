package com.atchurey.tools.talkativebot.core.channel;

import java.util.Objects;

/**
 * Routing decision for an inbound message before TalkativeBot checks pending interactions.
 *
 * <p>The address selects the external conversation target, the scope selects the internal workflow
 * lane for that address, and the mode controls whether TalkativeBot should resume, start, or reject.</p>
 */
public record ConversationRoute(
        ConversationAddress address,
        ConversationScope scope,
        ConversationRouteMode mode
) {

    public ConversationRoute {
        Objects.requireNonNull(address, "address must not be null");
        scope = scope == null ? ConversationScope.DEFAULT : scope;
        mode = mode == null ? ConversationRouteMode.RESUME_OR_START : mode;
    }

    /**
     * Route to the default scope and preserve the historical resume-or-start behavior.
     */
    public static ConversationRoute resumeOrStart(ConversationAddress address) {
        return new ConversationRoute(address, ConversationScope.DEFAULT, ConversationRouteMode.RESUME_OR_START);
    }

    /**
     * Route to a selected scope and resume if possible, otherwise start a new conversation.
     */
    public static ConversationRoute resumeOrStart(
            ConversationAddress address,
            ConversationScope scope
    ) {
        return new ConversationRoute(address, scope, ConversationRouteMode.RESUME_OR_START);
    }

    /**
     * Route to a selected scope and only resume an existing pending interaction.
     */
    public static ConversationRoute resumeOnly(
            ConversationAddress address,
            ConversationScope scope
    ) {
        return new ConversationRoute(address, scope, ConversationRouteMode.RESUME_ONLY);
    }

    /**
     * Route to a selected scope and start a new conversation without checking pending interactions.
     */
    public static ConversationRoute startOnly(
            ConversationAddress address,
            ConversationScope scope
    ) {
        return new ConversationRoute(address, scope, ConversationRouteMode.START_ONLY);
    }

    /**
     * Reject a message for the selected address before conversation resolution.
     */
    public static ConversationRoute reject(ConversationAddress address) {
        return new ConversationRoute(address, ConversationScope.DEFAULT, ConversationRouteMode.REJECT);
    }
}
