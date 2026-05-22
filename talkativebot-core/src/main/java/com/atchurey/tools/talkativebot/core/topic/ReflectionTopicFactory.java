package com.atchurey.tools.talkativebot.core.topic;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;

import java.lang.reflect.Constructor;

public class ReflectionTopicFactory implements TopicFactory {

    @Override
    public ConversationTopic createTopic(TopicDescriptor descriptor, Conversation<?> conversation) {
        Class<? extends ConversationTopic> topicType = descriptor.getTopicType();

        try {
            Constructor<? extends ConversationTopic> constructor = topicType.getConstructor(Conversation.class);
            return constructor.newInstance(conversation);
        } catch (NoSuchMethodException ignored) {
            return createWithDefaultConstructor(topicType);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create topic " + topicType.getName(), exception);
        }
    }

    private ConversationTopic createWithDefaultConstructor(Class<? extends ConversationTopic> topicType) {
        try {
            Constructor<? extends ConversationTopic> constructor = topicType.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "ConversationTopic must have either a no-args constructor or a constructor accepting Conversation: "
                            + topicType.getName(),
                    exception
            );
        }
    }
}