package com.atchurey.talkativebot.core.topic;

import com.atchurey.talkativebot.core.conversation.Conversation;

import java.util.List;

public interface TopicScanner {

	List<TopicDescriptor> scan(Class<? extends Conversation<?>> conversationType);
}