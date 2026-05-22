package com.atchurey.tools.talkativebot.core.conversation;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;

public interface ConversationFactory {

    Conversation<?> create(String conversationType, ConversationAddress address);
}