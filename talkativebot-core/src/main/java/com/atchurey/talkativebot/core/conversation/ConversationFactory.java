package com.atchurey.talkativebot.core.conversation;

import com.atchurey.talkativebot.core.channel.ConversationAddress;

public interface ConversationFactory {

    Conversation<?> create(String conversationType, ConversationAddress address);
}