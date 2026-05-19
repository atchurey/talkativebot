package com.atchurey.tools.talkativebot.core.conversation;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.TopicDescriptor;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractConversationTest {

    private TalkativeBot bot;
    private ConversationAddress address;
    private TopicScanner topicScanner;
    private TopicFactory topicFactory;
    private ConcreteConversation conversation;

    @BeforeEach
    void setUp() {
        bot = mock(TalkativeBot.class);
        address = new ConversationAddress("test", "user1", null, null);
        topicScanner = mock(TopicScanner.class);
        topicFactory = mock(TopicFactory.class);

        when(bot.getTopicScanner()).thenReturn(topicScanner);
        when(bot.getTopicFactory()).thenReturn(topicFactory);

        conversation = new ConcreteConversation(bot, address, topicScanner, topicFactory);
    }

    @Test
    void shouldDiscoverTopicsOnConstruction() {
        TopicDescriptor descriptor1 = createDescriptor("topic1", 1);
        TopicDescriptor descriptor2 = createDescriptor("topic2", 2);
        when(topicScanner.scan(any())).thenReturn(List.of(descriptor1, descriptor2));

        ConversationTopic topic1 = mock(ConversationTopic.class);
        when(topic1.getKey()).thenReturn("topic1");
        ConversationTopic topic2 = mock(ConversationTopic.class);
        when(topic2.getKey()).thenReturn("topic2");

        when(topicFactory.createTopic(eq(descriptor1), any())).thenReturn(topic1);
        when(topicFactory.createTopic(eq(descriptor2), any())).thenReturn(topic2);

        // Re-instantiate to trigger discovery
        conversation = new ConcreteConversation(bot, address, topicScanner, topicFactory);

        assertThat(conversation.getTopics()).contains(topic1, topic2);
        assertThat(conversation.getTopic("topic1")).contains(topic1);
        assertThat(conversation.getTopic("topic2")).contains(topic2);
    }

    @Test
    void shouldRegisterTopic() {
        ConversationTopic topic = mock(ConversationTopic.class);
        when(topic.getKey()).thenReturn("new-topic");

        conversation.registerTopic(topic);

        assertThat(conversation.getTopic("new-topic")).contains(topic);
    }

    @Test
    void shouldPlayTopicsInOrder() {
        ConversationTopic topic1 = mock(ConversationTopic.class);
        when(topic1.getKey()).thenReturn("t1");
        when(topic1.isPlayable(any())).thenReturn(true);

        ConversationTopic topic2 = mock(ConversationTopic.class);
        when(topic2.getKey()).thenReturn("t2");
        when(topic2.isPlayable(any())).thenReturn(true);

        conversation.registerTopic(topic1);
        conversation.registerTopic(topic2);

        // First play()
        conversation.play();
        verify(topic1, times(1)).play();
        assertThat(conversation.getCurrentTopic()).contains(topic1);
        
        // Mock topic1 as not playable anymore (simulating it finished)
        when(topic1.isPlayable(any())).thenReturn(false);
        
        // Second play()
        conversation.play();
        verify(topic2, times(1)).play();
        assertThat(conversation.getCurrentTopic()).contains(topic2);
        
        // Mock topic2 as not playable
        when(topic2.isPlayable(any())).thenReturn(false);
        
        // Third play()
        when(topic1.isPlayable(any())).thenReturn(false);
        when(topic2.isPlayable(any())).thenReturn(false);
        CompletableFuture<String> future3 = conversation.play();
        assertThat(future3.isDone()).isTrue();
        assertThat(future3.join()).isEqualTo("finished");
        assertThat(conversation.isClosed()).isTrue();
    }

    @Test
    void shouldReturnIncompleteFutureWhenTopicIsPlayable() {
        ConversationTopic topic1 = mock(ConversationTopic.class);
        when(topic1.getKey()).thenReturn("t1");
        when(topic1.isPlayable(any())).thenReturn(true);

        conversation.registerTopic(topic1);

        CompletableFuture<String> future = conversation.play();

        verify(topic1, times(1)).play();
        assertThat(future.isDone()).isFalse();
    }

    @Test
    void shouldAbandonConversation() {
        conversation.abandon();
        assertThat(conversation.isClosed()).isTrue();

        CompletableFuture<String> future = conversation.play();
        assertThat(future.isDone()).isTrue();
        assertThat(future.join()).isEqualTo("finished");
    }

    @Test
    void shouldHandleExplicitNextTopic() {
        ConversationTopic topic1 = mock(ConversationTopic.class);
        when(topic1.getKey()).thenReturn("t1");
        when(topic1.getNextTopicKey()).thenReturn("t3");
        when(topic1.isPlayable(any())).thenReturn(true);

        ConversationTopic topic2 = mock(ConversationTopic.class);
        when(topic2.getKey()).thenReturn("t2");
        when(topic2.isPlayable(any())).thenReturn(true);

        ConversationTopic topic3 = mock(ConversationTopic.class);
        when(topic3.getKey()).thenReturn("t3");
        when(topic3.isPlayable(any())).thenReturn(true);

        conversation.registerTopic(topic1);
        conversation.registerTopic(topic2);
        conversation.registerTopic(topic3);

        conversation.play(); // plays topic1
        verify(topic1).play();

        when(topic1.isPlayable(any())).thenReturn(false);

        conversation.play(); // plays topic3 (explicitly linked from topic1)
        verify(topic3).play();
        verify(topic2, times(0)).play();
    }

    private TopicDescriptor createDescriptor(String key, int order) {
        return new TopicDescriptor(key, key, key, false, ConcreteConversation.class, null, order, ConversationTopic.class);
    }

    private static class ConcreteConversation extends AbstractConversation<String> {
        private final CompletableFuture<String> topicPlayedFuture = new CompletableFuture<>();

        public ConcreteConversation(TalkativeBot bot, ConversationAddress address, TopicScanner topicScanner, TopicFactory topicFactory) {
            super(bot, address, topicScanner, topicFactory);
        }

        @Override
        protected CompletableFuture<String> onConversationClosed() {
            return CompletableFuture.completedFuture("finished");
        }

        @Override
        protected CompletableFuture<String> onAlreadyClosed() {
            return CompletableFuture.completedFuture("finished");
        }

        @Override
        protected CompletableFuture<String> onTopicPlayed(ConversationTopic topic) {
            return topicPlayedFuture;
        }
    }
}
