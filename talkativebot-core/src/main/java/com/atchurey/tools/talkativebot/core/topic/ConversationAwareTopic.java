package com.atchurey.talkativebot.core.topic;

import com.atchurey.talkativebot.core.conversation.Conversation;

public interface ConversationAwareTopic {

	void setConversation(Conversation<?> conversation);
}