package com.atchurey.tools.talkativebot.core.channel;

/**
 * Describes how a routed inbound message should interact with pending interaction lookup.
 */
public enum ConversationRouteMode {
    /**
     * Resume an existing pending interaction for the selected address and scope, or start a new conversation.
     */
    RESUME_OR_START,

    /**
     * Resume only if a pending interaction already exists for the selected address and scope.
     */
    RESUME_ONLY,

    /**
     * Start a new conversation without consuming any pending interaction for the address.
     */
    START_ONLY,

    /**
     * Reject the message before resume/start resolution.
     */
    REJECT
}
