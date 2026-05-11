package com.atchurey.talkativebot.core.topic;

import com.atchurey.talkativebot.core.conversation.Conversation;

public interface TopicFactory {

    ConversationTopic createTopic(TopicDescriptor descriptor, Conversation<?> conversation);
}