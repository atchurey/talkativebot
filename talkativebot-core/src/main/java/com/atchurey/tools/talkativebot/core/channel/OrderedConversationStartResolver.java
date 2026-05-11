package com.atchurey.tools.talkativebot.core.channel;

public interface OrderedConversationStartResolver extends ConversationStartResolver {
    int getOrder();
}
