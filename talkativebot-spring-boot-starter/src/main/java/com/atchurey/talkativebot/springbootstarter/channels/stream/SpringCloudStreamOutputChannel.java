package com.atchurey.talkativebot.springbootstarter.channels.stream;

import com.atchurey.talkativebot.core.channel.ConversationAddress;
import com.atchurey.talkativebot.core.channel.OutgoingMessage;
import com.atchurey.talkativebot.core.channel.OutputChannel;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.concurrent.CompletableFuture;

public class SpringCloudStreamOutputChannel implements OutputChannel {
    private final StreamBridge streamBridge;
    private final String bindingName;

    public SpringCloudStreamOutputChannel(
            StreamBridge streamBridge,
            String bindingName
    ) {
        this.streamBridge = streamBridge;
        this.bindingName = bindingName;
    }

    @Override
    public String name() {
        return "spring-cloud-stream";
    }

    @Override
    public boolean supports(ConversationAddress address) {
        return "spring-cloud-stream".equalsIgnoreCase(address.getChannel())
                || "stream".equalsIgnoreCase(address.getChannel());
    }

    @Override
    public CompletableFuture<Void> send(OutgoingMessage message) {
        boolean sent = streamBridge.send(bindingName, message);

        if (!sent) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Could not send message to binding " + bindingName)
            );
        }

        return CompletableFuture.completedFuture(null);
    }
}

