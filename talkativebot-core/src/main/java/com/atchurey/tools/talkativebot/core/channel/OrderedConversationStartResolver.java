package com.atchurey.talkativebot.core.channel;

public interface OrderedConversationStartResolver extends ConversationStartResolver {
    int getOrder();
}
