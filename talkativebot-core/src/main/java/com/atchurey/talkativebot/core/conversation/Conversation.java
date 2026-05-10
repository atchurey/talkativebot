package com.atchurey.talkativebot.core.conversation;

import com.atchurey.talkativebot.core.channel.ConversationAddress;
import com.atchurey.talkativebot.core.topic.ConversationTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Conversation<T> extends Serializable {

    Logger logger = LoggerFactory.getLogger(Conversation.class);

    CompletableFuture<T> play();

    boolean isClosed();

    ConversationTopic nextTopic();

    Optional<ConversationTopic> getCurrentTopic();

    Collection<ConversationTopic> getTopics();

    Optional<ConversationTopic> getTopic(String key);

    Facts getFacts();

    ConversationAddress getAddress();

    void abandon();
}