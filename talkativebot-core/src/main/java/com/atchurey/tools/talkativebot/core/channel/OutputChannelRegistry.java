package com.atchurey.tools.talkativebot.core.channel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class OutputChannelRegistry {

    private final List<OutputChannel> outputChannels;

    public OutputChannelRegistry(List<OutputChannel> outputChannels) {
        this.outputChannels = List.copyOf(Objects.requireNonNull(outputChannels, "outputChannels must not be null"));
    }

    public CompletableFuture<Void> send(OutgoingMessage message) {
        OutputChannel channel = outputChannels.stream()
                .filter(candidate -> candidate.supports(message.getAddress()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No output channel supports address channel: " + message.getAddress().getChannel()
                ));

        return channel.send(message);
    }
}
