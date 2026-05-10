package com.atchurey.talkativebot.springbootstarter.topic;

import com.atchurey.talkativebot.core.conversation.Conversation;
import com.atchurey.talkativebot.core.topic.ConversationAwareTopic;
import com.atchurey.talkativebot.core.topic.ConversationTopic;
import com.atchurey.talkativebot.core.topic.ReflectionTopicFactory;
import com.atchurey.talkativebot.core.topic.TopicDescriptor;
import com.atchurey.talkativebot.core.topic.TopicFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

public class SpringTopicFactory implements TopicFactory {

    private final ApplicationContext applicationContext;
    private final TopicFactory fallbackTopicFactory;

    public SpringTopicFactory(ApplicationContext applicationContext) {
        this(applicationContext, new ReflectionTopicFactory());
    }

    public SpringTopicFactory(
            ApplicationContext applicationContext,
            TopicFactory fallbackTopicFactory
    ) {
        this.applicationContext = applicationContext;
        this.fallbackTopicFactory = fallbackTopicFactory;
    }

    @Override
    public ConversationTopic createTopic(TopicDescriptor descriptor, Conversation<?> conversation) {
        ObjectProvider<? extends ConversationTopic> provider =
                applicationContext.getBeanProvider(descriptor.getTopicType());

        ConversationTopic topic = provider.getIfAvailable();

        if (topic != null) {
            if (topic instanceof ConversationAwareTopic conversationAwareTopic) {
                conversationAwareTopic.setConversation(conversation);
            }

            return topic;
        }

        return fallbackTopicFactory.createTopic(descriptor, conversation);
    }
}