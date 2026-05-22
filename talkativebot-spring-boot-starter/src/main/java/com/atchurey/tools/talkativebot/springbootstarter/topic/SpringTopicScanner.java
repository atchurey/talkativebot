package com.atchurey.tools.talkativebot.springbootstarter.topic;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.TopicDescriptor;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpringTopicScanner implements TopicScanner, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringTopicScanner.class);

    private final String basePackage;
    private ResourceLoader resourceLoader;

    public SpringTopicScanner(String basePackage) {
        this.basePackage = StringUtils.hasText(basePackage)
                ? basePackage
                : "com.atchurey.tools.talkative";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TopicDescriptor> scan(Class<? extends Conversation<?>> conversationType) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(Topic.class));

        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        return scanner.findCandidateComponents(basePackage)
                .stream()
                .map(beanDefinition -> ClassUtils.resolveClassName(
                        beanDefinition.getBeanClassName(),
                        Thread.currentThread().getContextClassLoader()
                ))
                .filter(ConversationTopic.class::isAssignableFrom)
                .map(topicClass -> (Class<? extends ConversationTopic>) topicClass)
                .map(this::toDescriptor)
                .filter(descriptor -> supportsConversation(descriptor, conversationType))
                .sorted(Comparator.comparingInt(TopicDescriptor::getOrder)
                        .thenComparing(TopicDescriptor::getKey))
                .collect(Collectors.toList());
    }

    private TopicDescriptor toDescriptor(Class<? extends ConversationTopic> topicType) {
        Topic context = AnnotationUtils.findAnnotation(topicType, Topic.class);

        if (context == null) {
            throw new IllegalStateException("ConversationTopic type is missing @Topic: " + topicType.getName());
        }

        String key = StringUtils.hasText(context.key())
                ? context.key()
                : Introspector.decapitalize(topicType.getSimpleName());

        String name = StringUtils.hasText(context.name())
                ? context.name()
                : topicType.getSimpleName();

        logger.debug("Discovered topic {} with key {}", topicType.getName(), key);

        return new TopicDescriptor(
                key,
                name,
                context.description(),
                context.canReplay(),
                context.conversation(),
                context.next(),
                context.order(),
                topicType
        );
    }

    private boolean supportsConversation(
            TopicDescriptor descriptor,
            Class<? extends Conversation<?>> conversationType
    ) {
        Class<?> configuredConversationType = descriptor.getConversationType();

        if (Conversation.class.equals(configuredConversationType)) {
            return true;
        }

        return configuredConversationType.isAssignableFrom(conversationType);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}