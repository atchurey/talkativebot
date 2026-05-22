package com.atchurey.tools.talkativebot.core.topic;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;

import java.util.List;

public interface TopicScanner {

	List<TopicDescriptor> scan(Class<? extends Conversation<?>> conversationType);
}