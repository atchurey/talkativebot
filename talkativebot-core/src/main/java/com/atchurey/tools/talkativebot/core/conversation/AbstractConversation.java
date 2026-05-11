package com.atchurey.tools.talkativebot.core.conversation;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.topic.ConversationAwareTopic;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.TopicDescriptor;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractConversation<T> implements Conversation<T> {

    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Getter
    protected final TalkativeBot bot;

    private final Facts facts = new Facts();

    private final ConversationAddress address;

    private final Map<String, ConversationTopic> topics = new LinkedHashMap<>();

    private ConversationTopic currentTopic;

    private boolean closed;

    private boolean abandoned;

    protected AbstractConversation(TalkativeBot bot, ConversationAddress address) {
        this.bot = Objects.requireNonNull(bot, "bot must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        discoverTopics(bot.getTopicScanner(), bot.getTopicFactory());
    }

    protected AbstractConversation(
            TalkativeBot bot,
            ConversationAddress address,
            TopicScanner topicScanner,
            TopicFactory topicFactory
    ) {
        this(bot, address);
        discoverTopics(topicScanner, topicFactory);
    }

	protected final void discoverTopics(TopicScanner topicScanner, TopicFactory topicFactory) {
        Objects.requireNonNull(topicScanner, "topicScanner must not be null");
        Objects.requireNonNull(topicFactory, "topicFactory must not be null");

        topicScanner.scan(conversationType())
                .stream()
                .sorted(Comparator.comparingInt(TopicDescriptor::getOrder)
                        .thenComparing(TopicDescriptor::getKey))
                .map(descriptor -> topicFactory.createTopic(descriptor, this))
                .forEach(this::registerTopic);
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Conversation<?>> conversationType() {
        return (Class<? extends Conversation<?>>) getClass();
    }

    protected final void registerTopic(ConversationTopic topic) {
        Objects.requireNonNull(topic, "topic must not be null");

        if (topic instanceof ConversationAwareTopic conversationAwareTopic) {
            conversationAwareTopic.setConversation(this);
        }

        if (!StringUtils.isNotBlank(topic.getKey())) {
            throw new IllegalArgumentException("ConversationTopic key must not be blank: " + topic.getClass().getName());
        }

        if (topics.containsKey(topic.getKey())) {
            throw new IllegalArgumentException("Duplicate topic key in conversation: " + topic.getKey());
        }

        topics.put(topic.getKey(), topic);

        logger.debug(
                "Registered topic {} [{}] in conversation {}",
                topic.getName(),
                topic.getKey(),
                getClass().getSimpleName()
        );
    }

    @Override
    public CompletableFuture<T> play() {
        if (isClosed()) {
            logger.debug("Conversation {} is already closed", getClass().getSimpleName());
            return onAlreadyClosed();
        }

        ConversationTopic topic = nextTopic();

        if (topic == null) {
            closeConversation();
            return onConversationClosed();
        }

        currentTopic = topic;

        logger.debug(
                "Playing topic {} [{}] in conversation {}",
                topic.getName(),
                topic.getKey(),
                getClass().getSimpleName()
        );

        topic.play();

        if (shouldCloseAfterTopic(topic)) {
            closeConversation();
            return onConversationClosed();
        }

        return onTopicPlayed(topic);
    }

    @Override
    public ConversationTopic nextTopic() {
        if (isClosed()) {
            return null;
        }

        ConversationTopic explicitNextTopic = resolveExplicitNextTopic();

        if (explicitNextTopic != null && explicitNextTopic.isPlayable(facts)) {
            return explicitNextTopic;
        }

        return topics.values()
                .stream()
                .filter(topic -> topic.isPlayable(facts))
                .sorted(Comparator.comparingInt(ConversationTopic::getOrder)
                        .thenComparing(ConversationTopic::getKey))
                .findFirst()
                .orElse(null);
    }

    private ConversationTopic resolveExplicitNextTopic() {
        if (currentTopic == null) {
            return null;
        }

        String nextTopicKey = currentTopic.getNextTopicKey();

        if (!StringUtils.isNotBlank(nextTopicKey)) {
            return null;
        }

        ConversationTopic nextTopic = topics.get(nextTopicKey);

        if (nextTopic == null) {
            logger.warn(
                    "ConversationTopic {} points to missing next topic {}",
                    currentTopic.getKey(),
                    nextTopicKey
            );
        }

        return nextTopic;
    }

    protected boolean shouldCloseAfterTopic(ConversationTopic topic) {
        return topics.values()
                .stream()
                .noneMatch(candidate -> candidate.isPlayable(facts));
    }

    public void onTopicClosed(ConversationTopic topic) {
        logger.debug(
                "ConversationTopic {} [{}] closed in conversation {}",
                topic.getName(),
                topic.getKey(),
                getClass().getSimpleName()
        );

        if (shouldCloseAfterTopic(topic)) {
            closeConversation();
            onConversationClosed();
        }
    }

    protected void closeConversation() {
        this.closed = true;

        logger.debug("Conversation {} closed", getClass().getSimpleName());
    }

    public boolean isTerminated() {
        return closed || abandoned;
    }

    @Override
    public boolean isClosed() {
        if (closed || abandoned) {
            return true;
        }

        if (topics.isEmpty()) {
            return false;
        }

        return topics.values()
                .stream()
                .noneMatch(topic -> topic.isPlayable(facts));
    }

    @Override
    public Optional<ConversationTopic> getCurrentTopic() {
        return Optional.ofNullable(currentTopic);
    }

    @Override
    public Collection<ConversationTopic> getTopics() {
        return Collections.unmodifiableCollection(topics.values());
    }

    @Override
    public Optional<ConversationTopic> getTopic(String key) {
        return Optional.ofNullable(topics.get(key));
    }

    @Override
    public Facts getFacts() {
        return facts;
    }

    @Override
    public ConversationAddress getAddress() {
        return address;
    }

    @Override
    public void abandon() {
        this.abandoned = true;
        this.closed = true;

        logger.debug("Conversation {} abandoned", getClass().getSimpleName());
    }

    protected CompletableFuture<T> onTopicPlayed(ConversationTopic topic) {
        return CompletableFuture.completedFuture(null);
    }

    protected CompletableFuture<T> onConversationClosed() {
        return CompletableFuture.completedFuture(null);
    }

    protected CompletableFuture<T> onAlreadyClosed() {
        return CompletableFuture.completedFuture(null);
    }
}