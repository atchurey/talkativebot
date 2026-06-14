package com.atchurey.tools.talkativebot.core.channel;

import java.util.Objects;

/**
 * Default router that preserves the original TalkativeBot behavior.
 *
 * <p>Every message is handled at its incoming address in {@link ConversationScope#DEFAULT}, resuming
 * an existing pending interaction when present or starting a new conversation otherwise.</p>
 */
public class DefaultConversationMessageRouter implements ConversationMessageRouter {

    @Override
    public ConversationRoute route(
            IncomingMessage message,
            ConversationRoutingContext context
    ) {
        Objects.requireNonNull(message, "message must not be null");
        return ConversationRoute.resumeOrStart(message.getAddress());
    }
}
