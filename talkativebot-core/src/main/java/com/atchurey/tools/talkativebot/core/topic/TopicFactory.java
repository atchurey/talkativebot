package com.atchurey.tools.talkativebot.core.topic;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;

public interface TopicFactory {

    ConversationTopic createTopic(TopicDescriptor descriptor, Conversation<?> conversation);
}