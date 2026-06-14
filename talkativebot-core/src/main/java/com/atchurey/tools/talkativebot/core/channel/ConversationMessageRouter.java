package com.atchurey.tools.talkativebot.core.channel;

/**
 * Selects the address, scope, and routing mode for an inbound message before pending lookup.
 *
 * <p>Applications can provide a custom router when one external address may have multiple workflow
 * lanes, such as checkout, payment, and order-status conversations for the same user.</p>
 */
public interface ConversationMessageRouter {

    /**
     * Routes an incoming message into the workflow lane TalkativeBot should use.
     *
     * @param message inbound message from an input integration
     * @param context read-only access to pending interactions by address and scope
     * @return routing decision; returning {@code null} falls back to default resume-or-start behavior
     */
    ConversationRoute route(
            IncomingMessage message,
            ConversationRoutingContext context
    );
}
