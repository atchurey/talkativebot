package com.atchurey.tools.talkativebot.core.channel;

import java.util.concurrent.CompletableFuture;

public interface OutputChannel {

    String name();

    boolean supports(ConversationAddress address);

    CompletableFuture<Void> send(OutgoingMessage message);
}
