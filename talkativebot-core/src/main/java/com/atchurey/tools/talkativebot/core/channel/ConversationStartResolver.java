package com.atchurey.tools.talkativebot.core.channel;

import java.util.Optional;

public interface ConversationStartResolver {

    Optional<ConversationStartRequest> resolve(IncomingMessage message);
}
