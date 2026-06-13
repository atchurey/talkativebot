package com.atchurey.tools.talkativebot.core.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ChannelInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String provider;
    private final String channel;
    private final String adapter;
    private final String conversationId;
    private final String sessionId;
    private final String messageId;

    @JsonCreator
    public ChannelInfo(
            @JsonProperty("provider") String provider,
            @JsonProperty("channel") String channel,
            @JsonProperty("adapter") String adapter,
            @JsonProperty("conversationId") String conversationId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("messageId") String messageId
    ) {
        this.provider = provider;
        this.channel = channel;
        this.adapter = adapter;
        this.conversationId = conversationId;
        this.sessionId = sessionId;
        this.messageId = messageId;
    }
}
