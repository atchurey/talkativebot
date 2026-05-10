package com.atchurey.talkativebot.core.channel;

public interface InputChannel {

    String name();

    void start(InputMessageHandler handler);

    void stop();
}
