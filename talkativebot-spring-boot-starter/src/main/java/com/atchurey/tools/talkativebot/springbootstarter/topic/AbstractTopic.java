package com.atchurey.tools.talkativebot.springbootstarter.topic;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.AbstractConversation;
import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.Facts;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.ConversationAwareTopic;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.TopicState;
import com.atchurey.tools.talkativebot.core.topic.interfaces.Topic;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.Introspector;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractTopic implements ConversationTopic, ConversationAwareTopic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 1L;
    private static final String TOPIC_CLOSED_FACT_PREFIX = "__talkative.topic.";
    private static final String TOPIC_CLOSED_FACT_SUFFIX = ".closed";

    protected Conversation<?> conversation;

    private TopicState state = TopicState.NEW;

    protected AbstractTopic() {
    }

    protected AbstractTopic(Conversation<?> conversation) {
        this.conversation = conversation;
    }

    @Override
    public void setConversation(Conversation<?> conversation) {
        this.conversation = conversation;
    }

	@Override
    public String getKey() {
        Topic context = topicContext();

        if (context != null && StringUtils.isNotBlank(context.key())) {
            return context.key();
        }

        return Introspector.decapitalize(getClass().getSimpleName());
    }

    @Override
    public String getName() {
        Topic context = topicContext();

        if (context != null && StringUtils.isNotBlank(context.name())) {
            return context.name();
        }

        return getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        Topic context = topicContext();
        return context == null ? "" : context.description();
    }

    @Override
    public int getOrder() {
        Topic context = topicContext();
        return context == null ? 0 : context.order();
    }

    @Override
    public String getNextTopicKey() {
        Topic context = topicContext();
        return context == null ? "" : context.next();
    }

    @Override
    public boolean canReplay() {
        Topic context = topicContext();
        return context != null && context.canReplay();
    }

    @Override
    public boolean isPlayable(@NonNull Facts facts) {
        Objects.requireNonNull(facts, "facts must not be null");

        if (conversation == null /*|| conversation.isClosed()*/) {
            return false;
        }

        if (conversation instanceof AbstractConversation<?> abstractConversation
                && abstractConversation.isTerminated()) {
            return false;
        }

        if (isClosed() && !canReplay()) {
            return false;
        }

        return canPlayWithFacts(facts);
    }

    protected boolean canPlayWithFacts(@NonNull Facts facts) {
        return true;
    }

    @Override
    public final void play() {
        if (conversation == null) {
            throw new IllegalStateException("ConversationTopic has no associated conversation: " + getClass().getName());
        }

        if (!isPlayable(conversation.getFacts())) {
            logger.debug("ConversationTopic {} is not playable", getKey());
            return;
        }

        state = TopicState.PLAYING;

        try {
            doPlay(conversation.getFacts());
        } catch (RuntimeException exception) {
            state = TopicState.FAILED;
            throw exception;
        }
    }

    protected abstract void doPlay(Facts facts);

    @Override
    public boolean isClosed() {
        if (TopicState.CLOSED.equals(state)) {
            return true;
        }

        if (conversation == null) {
            return false;
        }

        Boolean closedFromFacts = conversation.getFacts().get(closedFactKey());
        return Boolean.TRUE.equals(closedFromFacts);
    }

    public boolean isPlaying() {
        return TopicState.PLAYING.equals(state);
    }

    public boolean isFailed() {
        return TopicState.FAILED.equals(state);
    }

    public TopicState getState() {
        return state;
    }

    protected void markPlaying() {
        this.state = TopicState.PLAYING;
    }

    protected void markSkipped() {
        this.state = TopicState.SKIPPED;
    }

    protected void markFailed() {
        this.state = TopicState.FAILED;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }

        this.state = TopicState.CLOSED;

        if (conversation != null) {
            conversation.getFacts().put(closedFactKey(), true);
        }

        if (conversation instanceof AbstractConversation<?> abstractConversation) {
            abstractConversation.onTopicClosed(this);
        }
    }

    @Override
    public void reset() {
        this.state = TopicState.NEW;

        if (conversation != null) {
            conversation.getFacts().remove(closedFactKey());
        }
    }

    protected <T> T fact(String name) {
        return conversation.getFacts().get(name);
    }

    protected <T> void fact(String name, T value) {
        conversation.getFacts().put(name, value);
    }

    protected TalkativeBot getBot() {
        if (conversation instanceof AbstractConversation<?> abstractConversation) {
            return abstractConversation.getBot();
        }

        throw new IllegalStateException("Conversation does not expose TalkativeBot: " + conversation);
    }

    private Topic topicContext() {
        return AnnotationUtils.findAnnotation(getClass(), Topic.class);
    }

    @Override
    public void onInput(SelectedAnswer selectedAnswer) {
        close();
    }

    private String closedFactKey() {
        return TOPIC_CLOSED_FACT_PREFIX + getKey() + TOPIC_CLOSED_FACT_SUFFIX;
    }

    //Helper methods
    protected CompletableFuture<Void> ask(Question question) {
        return getBot().ask(
                conversation,
                this,
                question
        );
    }

    protected CompletableFuture<String> reply(String text) {
        return getBot().reply(text);
    }

    protected void abandonConversation() {
        this.conversation.abandon();
    }
}