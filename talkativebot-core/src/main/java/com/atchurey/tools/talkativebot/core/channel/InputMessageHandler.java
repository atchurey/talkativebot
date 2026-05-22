package com.atchurey.tools.talkativebot.core.channel;

import java.util.concurrent.CompletableFuture;

public interface InputMessageHandler {

    CompletableFuture<Void> handle(IncomingMessage message);
}
