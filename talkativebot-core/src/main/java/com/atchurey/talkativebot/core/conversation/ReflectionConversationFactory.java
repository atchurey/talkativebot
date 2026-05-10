package com.atchurey.talkativebot.core.conversation;

import com.atchurey.talkativebot.core.bot.TalkativeBot;
import com.atchurey.talkativebot.core.channel.ConversationAddress;
import org.springframework.beans.factory.ObjectProvider;

public class ReflectionConversationFactory implements ConversationFactory {

    private final ObjectProvider<TalkativeBot> botProvider;

    public ReflectionConversationFactory(ObjectProvider<TalkativeBot> botProvider) {
        this.botProvider = botProvider;
    }

    @Override
    public Conversation<?> create(String conversationType, ConversationAddress address) {
        try {
            Class<?> type = Class.forName(conversationType);

            if (!Conversation.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(conversationType + " does not implement Conversation");
            }

            return (Conversation<?>) type
                    .getConstructor(TalkativeBot.class, ConversationAddress.class)
                    .newInstance(botProvider.getObject(), address);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create conversation " + conversationType, exception);
        }
    }
}