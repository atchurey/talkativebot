package com.atchurey.tools.talkativebot.core.bot;

import com.atchurey.tools.talkativebot.core.channel.*;
import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import com.atchurey.tools.talkativebot.core.conversation.*;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.store.InMemoryPendingInteractionStore;
import com.atchurey.tools.talkativebot.core.topic.ConversationAwareTopic;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.ReflectionTopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicDescriptor;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TalkativeBotTest {

    private TalkativeBot bot;
    private PendingInteractionStore store;
    private OutputChannelRegistry outputChannelRegistry;
    private ConversationStartRegistry startRegistry;
    private ConversationFactory conversationFactory;
    private TopicScanner topicScanner;

    @BeforeEach
    void setUp() {
        TalkativeBotProperties properties = new TalkativeBotProperties();
        store = new InMemoryPendingInteractionStore();
        outputChannelRegistry = mock(OutputChannelRegistry.class);
        when(outputChannelRegistry.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        topicScanner = mock(TopicScanner.class);
        startRegistry = mock(ConversationStartRegistry.class);
        conversationFactory = mock(ConversationFactory.class);

        bot = createBot(new DefaultConversationMessageRouter());
    }

    private TestConversation lastConversation;

    @Test
    void shouldHandleFullConversationFlow() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);

        // 1. Initial message starts conversation
        IncomingMessage firstMessage = new IncomingMessage(address, "hello");
        ConversationStartRequest startRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "hello", Collections.emptyMap());

        when(startRegistry.resolve(firstMessage, ConversationScope.DEFAULT)).thenReturn(Optional.of(startRequest));

        // Mock topic scanning for TestConversation
        TopicDescriptor startTopicDesc = new TopicDescriptor("start", "start", "", false, TestConversation.class, "end", 0, StartTopic.class);
        TopicDescriptor endTopicDesc = new TopicDescriptor("end", "end", "", false, TestConversation.class, null, 1, EndTopic.class);
        when(topicScanner.scan(eq(TestConversation.class))).thenReturn(List.of(startTopicDesc, endTopicDesc));

        when(conversationFactory.create(eq(TestConversation.class.getName()), eq(address)))
                .thenAnswer(invocation -> {
                    lastConversation = new TestConversation(bot, address);
                    return lastConversation;
                });

        bot.handle(firstMessage).join();

        // Verify interaction saved
        assertThat(store.findByAddress(address)).isPresent();
        PendingInteraction savedInteraction = store.findByAddress(address).get();
        assertThat(savedInteraction.getCurrentTopicKey()).isEqualTo("start");

        // 2. Second message resumes conversation
        IncomingMessage secondMessage = new IncomingMessage(address, "Alice");

        bot.handle(secondMessage).join();

        // Verify EndTopic was played and greeting sent
        ArgumentCaptor<OutgoingMessage> messageCaptor = ArgumentCaptor.forClass(OutgoingMessage.class);
        verify(outputChannelRegistry, times(2)).send(messageCaptor.capture());
        List<OutgoingMessage> sentMessages = messageCaptor.getAllValues();
        
        assertThat(sentMessages.get(0).getQuestion().getText()).isEqualTo("What is your name?");
        assertThat(sentMessages.get(1).getText()).isEqualTo("Hello Alice");

        // Verify terminal output did not leave a pending interaction.
        assertThat(store.findByAddress(address)).isEmpty();

        assertThat(lastConversation.isClosed()).isTrue();
    }

    @Test
    void shouldConcludeWithoutSavingPendingInteraction() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);
        TestConversation conversation = new TestConversation(bot, address);

        bot.conclude(conversation, "Command completed.").join();

        ArgumentCaptor<OutgoingMessage> messageCaptor = ArgumentCaptor.forClass(OutgoingMessage.class);
        verify(outputChannelRegistry).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getAddress()).isEqualTo(address);
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Command completed.");
        assertThat(store.findByAddress(address)).isEmpty();
        assertThat(conversation.isClosed()).isTrue();
    }

    @Test
    void shouldHandleNoConversationStarted() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);
        IncomingMessage message = new IncomingMessage(address, "unknown");

        when(startRegistry.resolve(message, ConversationScope.DEFAULT)).thenReturn(Optional.empty());

        bot.handle(message).join();

        ArgumentCaptor<OutgoingMessage> messageCaptor = ArgumentCaptor.forClass(OutgoingMessage.class);
        verify(outputChannelRegistry).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).contains("I don't know how to start a conversation");

        // Verify no interaction was saved
        assertThat(store.findByAddress(address)).isEmpty();
    }

    @Test
    void shouldPropagateErrorWhenNoStartResolverMatchedAndSendingFails() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);
        IncomingMessage message = new IncomingMessage(address, "unknown");

        when(startRegistry.resolve(message, ConversationScope.DEFAULT)).thenReturn(Optional.empty());
        when(outputChannelRegistry.send(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Send failed")));

        CompletableFuture<Void> result = bot.handle(message);

        assertThat(result).isCompletedExceptionally();
    }

    @Test
    void shouldKeepPlayingTopicWhenNotClosedOnInput() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);

        // 1. Initial message starts conversation
        IncomingMessage firstMessage = new IncomingMessage(address, "hello");
        ConversationStartRequest startRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "hello", Collections.emptyMap());

        when(startRegistry.resolve(firstMessage, ConversationScope.DEFAULT)).thenReturn(Optional.of(startRequest));

        // Mock topic scanning
        TopicDescriptor multiTopicDesc = new TopicDescriptor("multi", "multi", "", false, TestConversation.class, null, 0, MultiStepTopic.class);
        when(topicScanner.scan(eq(TestConversation.class))).thenReturn(List.of(multiTopicDesc));

        when(conversationFactory.create(eq(TestConversation.class.getName()), eq(address)))
                .thenAnswer(invocation -> {
                    lastConversation = new TestConversation(bot, address);
                    return lastConversation;
                });

        bot.handle(firstMessage).join();

        // Verify first question asked
        ArgumentCaptor<OutgoingMessage> messageCaptor = ArgumentCaptor.forClass(OutgoingMessage.class);
        verify(outputChannelRegistry, times(1)).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getQuestion().getText()).isEqualTo("Step 1?");

        // 2. Second message (input for Step 1)
        IncomingMessage secondMessage = new IncomingMessage(address, "answer 1");
        bot.handle(secondMessage).join();

        // Verify second question asked (because topic didn't close and was played again)
        verify(outputChannelRegistry, times(2)).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getQuestion().getText()).isEqualTo("Step 2?");

        // Interaction should still be there for "multi" topic
        assertThat(store.findByAddress(address)).isPresent();
        assertThat(store.findByAddress(address).get().getCurrentTopicKey()).isEqualTo("multi");

        // 3. Third message (input for Step 2)
        IncomingMessage thirdMessage = new IncomingMessage(address, "answer 2");
        bot.handle(thirdMessage).join();

        // Topic should now be closed
        assertThat(lastConversation.isClosed()).isTrue();
        assertThat(store.findByAddress(address)).isEmpty();
    }

    @Test
    void shouldKeepConversationOpenWhenTopicCanReplay() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);

        IncomingMessage message = new IncomingMessage(address, "start");
        ConversationStartRequest startRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "start", Collections.emptyMap());

        when(startRegistry.resolve(message, ConversationScope.DEFAULT)).thenReturn(Optional.of(startRequest));

        TopicDescriptor replayTopicDesc = new TopicDescriptor("replay", "replay", "", true, TestConversation.class, null, 0, ReplayableTopic.class);
        when(topicScanner.scan(eq(TestConversation.class))).thenReturn(List.of(replayTopicDesc));

        when(conversationFactory.create(eq(TestConversation.class.getName()), eq(address)))
                .thenAnswer(invocation -> {
                    lastConversation = new TestConversation(bot, address);
                    return lastConversation;
                });

        // Play the topic once (starts conversation)
        bot.handle(message).join();

        // Topic is NOT closed yet, it just asked a question
        assertThat(lastConversation.getTopic("replay").get().isClosed()).isFalse();
        assertThat(store.findByAddress(address)).isPresent();

        // Send input to close the topic
        IncomingMessage secondMessage = new IncomingMessage(address, "yes");
        bot.handle(secondMessage).join();

        // Topic is now closed (at least once) but it's replayable
        assertThat(lastConversation.getTopic("replay").get().isClosed()).isTrue();
        assertThat(lastConversation.isClosed()).isFalse(); // Conversation should still be open because topic is replayable
        
        // It should have played again and saved a new interaction
        assertThat(store.findByAddress(address)).isPresent();
        assertThat(store.findByAddress(address).get().getCurrentTopicKey()).isEqualTo("replay");

        // Although interaction is deleted, the conversation itself (if we had it in memory) is not closed.
        // In a stateless environment, we rely on PendingInteractionStore.
        // If the topic is closed, TalkativeBot.ask is not called again in onInput, so no new interaction is saved.
    }

    @Test
    void shouldCloseConversationWhenAbandonedEvenIfTopicCanReplay() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);

        TestConversation conversation = new TestConversation(bot, address);
        ReplayableTopic topic = new ReplayableTopic(conversation);
        conversation.addTopic(topic);

        assertThat(topic.isPlayable(conversation.getFacts())).isTrue();
        assertThat(conversation.isClosed()).isFalse();

        topic.close();
        assertThat(topic.isClosed()).isTrue();
        assertThat(topic.isPlayable(conversation.getFacts())).isTrue(); // Still playable because canReplay = true
        assertThat(conversation.isClosed()).isFalse(); // Still open

        conversation.abandon();
        assertThat(conversation.isClosed()).isTrue();
        assertThat(topic.isPlayable(conversation.getFacts())).isFalse(); // Now NOT playable because terminated
    }

    @Test
    void shouldSaveAndResumePendingInteractionInRoutedScope() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);
        ConversationScope checkoutScope = ConversationScope.of("checkout");
        bot = createBot((message, context) -> ConversationRoute.resumeOrStart(message.getAddress(), checkoutScope));

        IncomingMessage firstMessage = new IncomingMessage(address, "hello");
        ConversationStartRequest startRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "hello", Collections.emptyMap());

        when(startRegistry.resolve(firstMessage, checkoutScope)).thenReturn(Optional.of(startRequest));

        TopicDescriptor startTopicDesc = new TopicDescriptor("start", "start", "", false, TestConversation.class, "end", 0, StartTopic.class);
        TopicDescriptor endTopicDesc = new TopicDescriptor("end", "end", "", false, TestConversation.class, null, 1, EndTopic.class);
        when(topicScanner.scan(eq(TestConversation.class))).thenReturn(List.of(startTopicDesc, endTopicDesc));

        when(conversationFactory.create(eq(TestConversation.class.getName()), eq(address)))
                .thenAnswer(invocation -> {
                    lastConversation = new TestConversation(bot, address);
                    return lastConversation;
                });

        bot.handle(firstMessage).join();

        assertThat(store.findByAddress(address)).isEmpty();
        assertThat(store.findByAddress(address, checkoutScope)).isPresent();

        IncomingMessage secondMessage = new IncomingMessage(address, "Ada");
        bot.handle(secondMessage).join();

        assertThat(store.findByAddress(address)).isEmpty();
        assertThat(store.findByAddress(address, checkoutScope)).isEmpty();
    }

    @Test
    void shouldStartOnlyWithoutConsumingExistingPendingInteraction() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);
        bot = createBot((message, context) -> {
            if ("/status".equals(message.getText())) {
                return ConversationRoute.startOnly(message.getAddress(), ConversationScope.of("status"));
            }
            return ConversationRoute.resumeOrStart(message.getAddress());
        });

        IncomingMessage firstMessage = new IncomingMessage(address, "hello");
        ConversationStartRequest checkoutStartRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "hello", Collections.emptyMap());

        when(startRegistry.resolve(firstMessage, ConversationScope.DEFAULT)).thenReturn(Optional.of(checkoutStartRequest));

        TopicDescriptor startTopicDesc = new TopicDescriptor("start", "start", "", false, TestConversation.class, "end", 0, StartTopic.class);
        TopicDescriptor endTopicDesc = new TopicDescriptor("end", "end", "", false, TestConversation.class, null, 1, EndTopic.class);
        when(topicScanner.scan(eq(TestConversation.class))).thenReturn(List.of(startTopicDesc, endTopicDesc));

        when(conversationFactory.create(eq(TestConversation.class.getName()), eq(address)))
                .thenAnswer(invocation -> {
                    lastConversation = new TestConversation(bot, address);
                    return lastConversation;
                });

        bot.handle(firstMessage).join();
        assertThat(store.findByAddress(address)).isPresent();

        IncomingMessage statusMessage = new IncomingMessage(address, "/status");
        ConversationStartRequest statusStartRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "/status", Collections.emptyMap());
        when(startRegistry.resolve(statusMessage, ConversationScope.of("status"))).thenReturn(Optional.of(statusStartRequest));

        bot.handle(statusMessage).join();

        assertThat(store.findByAddress(address)).isPresent();
        assertThat(store.findByAddress(address).get().getCurrentTopicKey()).isEqualTo("start");
        assertThat(store.findByAddress(address, ConversationScope.of("status"))).isPresent();
    }

    @Test
    void shouldNotUseDefaultScopeStartResolverForNonDefaultRoute() {
        ConversationAddress address = new ConversationAddress("test", "user123", null, null);
        ConversationScope statusScope = ConversationScope.of("status");
        bot = createBot((message, context) -> ConversationRoute.startOnly(message.getAddress(), statusScope));

        IncomingMessage statusMessage = new IncomingMessage(address, "/status");
        ConversationStartRequest checkoutStartRequest = new ConversationStartRequest(
                address, TestConversation.class.getName(), "/status", Collections.emptyMap());
        when(startRegistry.resolve(statusMessage, ConversationScope.DEFAULT)).thenReturn(Optional.of(checkoutStartRequest));
        when(startRegistry.resolve(statusMessage, statusScope)).thenReturn(Optional.empty());

        bot.handle(statusMessage).join();

        verify(startRegistry).resolve(statusMessage, statusScope);
        verify(startRegistry, never()).resolve(statusMessage, ConversationScope.DEFAULT);
        assertThat(store.findByAddress(address)).isEmpty();
        assertThat(store.findByAddress(address, statusScope)).isEmpty();
    }

    private TalkativeBot createBot(ConversationMessageRouter messageRouter) {
        ConversationRuntimeRegistry runtimeRegistry = new ConversationRuntimeRegistry();
        ConversationRuntimeInitializerResolver runtimeInitializerResolver = new DefaultConversationRuntimeInitializerResolver(Collections.emptyList());

        return new TalkativeBot(
                new TalkativeBotProperties(),
                store,
                topicScanner,
                new ReflectionTopicFactory(),
                outputChannelRegistry,
                new DefaultOptionSelector(),
                conversationFactory,
                startRegistry,
                runtimeRegistry,
                runtimeInitializerResolver,
                messageRouter
        );
    }

    public static class TestConversation extends AbstractConversation<Void> {
        public TestConversation(TalkativeBot bot, ConversationAddress address) {
            super(bot, address);
        }
        public void addTopic(ConversationTopic topic) {
            registerTopic(topic);
        }
    }

    public static abstract class BaseTopic implements ConversationTopic, ConversationAwareTopic {
        private final String key;
        protected Conversation<?> conversation;
        private boolean closed;

        public BaseTopic(String key) {
            this.key = key;
        }

        @Override public String getKey() { return key; }
        @Override public String getName() { return key; }
        @Override public String getDescription() { return ""; }
        @Override public int getOrder() { return 0; }
        @Override public String getNextTopicKey() { return null; }
        @Override public boolean canReplay() { return false; }
        @Override public boolean isPlayable(Facts facts) {
            if (conversation instanceof AbstractConversation<?> abstractConversation
                    && abstractConversation.isTerminated()) {
                return false;
            }
            return !closed || canReplay();
        }
        @Override public boolean isClosed() { return closed; }
        @Override public void close() {
            this.closed = true;
            if (conversation instanceof AbstractConversation<?> abstractConversation) {
                abstractConversation.onTopicClosed(this);
            }
        }
        @Override public void reset() { this.closed = false; }
        @Override public void setConversation(Conversation<?> conversation) { this.conversation = conversation; }
        @Override public void onInput(SelectedAnswer selectedAnswer) { close(); }
    }

    public static class ReplayableTopic extends BaseTopic {
        public ReplayableTopic(Conversation<?> conversation) {
            super("replay");
            setConversation(conversation);
        }
        @Override public boolean canReplay() { return true; }
        @Override public void play() {
            ((AbstractConversation<?>) conversation).getBot().ask(conversation, this, Question.text("Replay?"));
        }
    }

    public static class MultiStepTopic extends BaseTopic {
        public MultiStepTopic(Conversation<?> conversation) {
            super("multi");
            setConversation(conversation);
        }

        @Override
        public void play() {
            Integer step = (Integer) conversation.getFacts().get("step");
            if (step == null) {
                ((AbstractConversation<?>) conversation).getBot().ask(conversation, this, Question.text("Step 1?"));
            } else if (step == 1) {
                ((AbstractConversation<?>) conversation).getBot().ask(conversation, this, Question.text("Step 2?"));
            }
        }

        @Override
        public void onInput(SelectedAnswer answer) {
            Integer step = (Integer) conversation.getFacts().get("step");
            if (step == null) {
                conversation.getFacts().put("step", 1);
                // DO NOT CLOSE
            } else {
                close();
            }
        }
    }

    public static class StartTopic extends BaseTopic {
        public StartTopic(Conversation<?> conversation) {
            super("start");
            setConversation(conversation);
        }
        @Override
        public void play() {
            ((AbstractConversation<?>) conversation).getBot().ask(conversation, this, Question.text("What is your name?"));
        }
        @Override
        public void onInput(SelectedAnswer answer) {
            conversation.getFacts().put("name", answer.getText());
            super.onInput(answer);
        }
        @Override
        public String getNextTopicKey() {
            return "end";
        }
    }

    public static class EndTopic extends BaseTopic {
        public EndTopic(Conversation<?> conversation) {
            super("end");
            setConversation(conversation);
        }
        @Override
        public void play() {
            ((AbstractConversation<?>) conversation).getBot().conclude(conversation, "Hello " + conversation.getFacts().get("name"));
        }
        @Override
        public int getOrder() {
            return 1;
        }
    }
}
