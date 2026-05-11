package com.example.console.conversations;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.conversation.AbstractConversation;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;

import java.util.concurrent.CompletableFuture;

/**
* A standard conversation declaration, overriding probably the two most useful callbacks, onTopicPlayed and onConversationClosed.
 *
 * In onConversationClosed, you can collect the final facts (details) of the conversation.
* */
public class SaleConversation extends AbstractConversation<String> {

    public SaleConversation(TalkativeBot bot, ConversationAddress address) {
        super(bot, address);
    }

    @Override
    protected CompletableFuture<String> onTopicPlayed(ConversationTopic topic) {
        if (isClosed()) {
            return onConversationClosed();
        }

        return CompletableFuture.completedFuture("Played topic: " + topic.getKey());
    }

    @Override
    protected CompletableFuture<String> onConversationClosed() {
        Facts checkoutDetails =  getFacts();
        logger.info("Checkout details: {}", checkoutDetails);
        return CompletableFuture.completedFuture("Conversation closed");
    }

}