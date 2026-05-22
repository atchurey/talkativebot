package com.atchurey.tools.talkativebot.core.topic;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;

public interface ConversationAwareTopic {

	void setConversation(Conversation<?> conversation);
}