package com.atchurey.tools.talkativebot.core.conversation;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;

import java.util.function.Supplier;

public class ReflectionConversationFactory implements ConversationFactory {

    private final Supplier<TalkativeBot> botSupplier;

    public ReflectionConversationFactory(Supplier<TalkativeBot> botSupplier) {
        this.botSupplier = botSupplier;
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
                    .newInstance(botSupplier.get(), address);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create conversation " + conversationType, exception);
        }
    }
}