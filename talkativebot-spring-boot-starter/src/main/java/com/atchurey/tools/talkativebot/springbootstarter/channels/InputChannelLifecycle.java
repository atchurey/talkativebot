package com.atchurey.tools.talkativebot.springbootstarter.channels;

import com.atchurey.tools.talkativebot.core.bot.Talkativebot;
import com.atchurey.tools.talkativebot.core.channel.InputChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class InputChannelLifecycle implements SmartLifecycle {

    private final Talkativebot talkativeBot;
    private final List<InputChannel> inputChannels;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            inputChannels.forEach(inputChannel -> inputChannel.start(talkativeBot));
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            inputChannels.forEach(InputChannel::stop);
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
