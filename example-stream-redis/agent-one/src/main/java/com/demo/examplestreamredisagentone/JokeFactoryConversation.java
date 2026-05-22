package com.demo.examplestreamredisagentone;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.conversation.AbstractConversation;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;

import java.util.concurrent.CompletableFuture;

public class JokeFactoryConversation extends AbstractConversation<String> {

    public JokeFactoryConversation(TalkativeBot bot, ConversationAddress address) {
        super(bot, address);
    }

    @Override
    protected CompletableFuture<String> onTopicPlayed(ConversationTopic topic) {
        if (isClosed()) {
            return onConversationClosed();
        }

        return CompletableFuture.completedFuture("Played topic: " + topic.getKey());
    }

    public JokeFactoryRuntime jokeFactoryRuntime() {
        return getRuntime().get(
                JokeFactoryRuntimeKeys.RUNTIME,
                JokeFactoryRuntime.class
        );
    }

    @Override
    protected CompletableFuture<String> onConversationClosed() {
        return CompletableFuture.completedFuture("Conversation closed");
    }

}