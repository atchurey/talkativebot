package com.atchurey.tools.talkativebot.springbootstarter.topic;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.topic.ConversationAwareTopic;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.ReflectionTopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicDescriptor;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
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