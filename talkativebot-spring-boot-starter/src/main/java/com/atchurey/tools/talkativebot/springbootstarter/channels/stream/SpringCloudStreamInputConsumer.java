package com.atchurey.tools.talkativebot.springbootstarter.channels.stream;

import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import com.atchurey.tools.talkativebot.core.channel.InputMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SpringCloudStreamInputConsumer implements Consumer<IncomingMessage> {
    private static final Logger logger = LoggerFactory.getLogger(SpringCloudStreamInputConsumer.class);

    private final InputMessageHandler inputMessageHandler;

    public SpringCloudStreamInputConsumer(InputMessageHandler inputMessageHandler) {
        this.inputMessageHandler = inputMessageHandler;
    }

    @Override
    public void accept(IncomingMessage incomingMessage) {
        inputMessageHandler.handle(incomingMessage)
                .whenComplete((ignored, exception) -> {
                    if (exception != null) {
                        logger.error("Could not handle incoming stream message {}", incomingMessage.getId(), exception);
                    }
                });
    }
}

