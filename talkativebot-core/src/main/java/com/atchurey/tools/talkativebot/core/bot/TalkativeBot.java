package com.atchurey.tools.talkativebot.core.bot;

import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationMessageRouter;
import com.atchurey.tools.talkativebot.core.channel.ConversationRoute;
import com.atchurey.tools.talkativebot.core.channel.ConversationRouteMode;
import com.atchurey.tools.talkativebot.core.channel.ConversationRoutingContext;
import com.atchurey.tools.talkativebot.core.channel.ConversationScope;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRegistry;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRequest;
import com.atchurey.tools.talkativebot.core.channel.DefaultConversationMessageRouter;
import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import com.atchurey.tools.talkativebot.core.channel.InputMessageHandler;
import com.atchurey.tools.talkativebot.core.channel.OptionSelector;
import com.atchurey.tools.talkativebot.core.channel.OutgoingMessage;
import com.atchurey.tools.talkativebot.core.channel.OutputChannelRegistry;
import com.atchurey.tools.talkativebot.core.channel.PendingInteraction;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.core.channel.SelectedAnswer;
import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.ConversationFactory;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntime;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializerResolver;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeRegistry;
import com.atchurey.tools.talkativebot.core.conversation.RuntimeAwareConversation;
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.ReflectionTopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import lombok.Getter;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TalkativeBot implements InputMessageHandler {

    @Getter
    private final TopicScanner topicScanner;

    @Getter
    private final TopicFactory topicFactory;

    private final TalkativeBotProperties botProperties;
    private final PendingInteractionStore pendingInteractionStore;
    private final OutputChannelRegistry outputChannelRegistry;
    private final OptionSelector optionSelector;
    private final ConversationFactory conversationFactory;
    private final Duration pendingInteractionTtl;
    private final ConversationStartRegistry conversationStartRegistry;
    private final ConversationRuntimeRegistry runtimeRegistry;
    private final ConversationRuntimeInitializerResolver runtimeInitializerResolver;
    private final ConversationMessageRouter conversationMessageRouter;
    private final ConversationRoutingContext conversationRoutingContext;
    // The router chooses a scope while handling one inbound message. Topic code can later call ask(...)
    // during that same call stack, but ask(...) is not passed the scope directly. This ThreadLocal lets
    // ask(...) read the scope for the current handling thread, without mixing it with another message
    // being handled at the same time.
    private final ThreadLocal<ConversationScope> activeScope = ThreadLocal.withInitial(() -> ConversationScope.DEFAULT);

    public TalkativeBot(
            TalkativeBotProperties botProperties,
            PendingInteractionStore pendingInteractionStore,
            TopicScanner topicScanner,
            TopicFactory topicFactory,
            OutputChannelRegistry outputChannelRegistry,
            OptionSelector optionSelector,
            ConversationFactory conversationFactory,
            ConversationStartRegistry conversationStartRegistry,
            ConversationRuntimeRegistry runtimeRegistry,
            ConversationRuntimeInitializerResolver runtimeInitializerResolver) {
        this(
                botProperties,
                pendingInteractionStore,
                topicScanner,
                topicFactory,
                outputChannelRegistry,
                optionSelector,
                conversationFactory,
                conversationStartRegistry,
                runtimeRegistry,
                runtimeInitializerResolver,
                new DefaultConversationMessageRouter()
        );
    }

    public TalkativeBot(
            TalkativeBotProperties botProperties,
            PendingInteractionStore pendingInteractionStore,
            TopicScanner topicScanner,
            TopicFactory topicFactory,
            OutputChannelRegistry outputChannelRegistry,
            OptionSelector optionSelector,
            ConversationFactory conversationFactory,
            ConversationStartRegistry conversationStartRegistry,
            ConversationRuntimeRegistry runtimeRegistry,
            ConversationRuntimeInitializerResolver runtimeInitializerResolver,
            ConversationMessageRouter conversationMessageRouter) {

        this.botProperties = botProperties;
        this.pendingInteractionStore = pendingInteractionStore;
        this.topicScanner = topicScanner;
        this.topicFactory = topicFactory == null ? new ReflectionTopicFactory() : topicFactory;
        this.outputChannelRegistry = outputChannelRegistry;
        this.optionSelector = optionSelector;
        this.conversationFactory = conversationFactory;
        this.pendingInteractionTtl = botProperties.getPendingInteraction().getTtl();
        this.conversationStartRegistry = conversationStartRegistry;
        this.runtimeRegistry = runtimeRegistry;
        this.runtimeInitializerResolver = runtimeInitializerResolver;
        this.conversationMessageRouter = conversationMessageRouter == null
                ? new DefaultConversationMessageRouter()
                : conversationMessageRouter;
        this.conversationRoutingContext = pendingInteractionStore::findByAddress;
    }

    public TalkativeBotProperties getBotConfigProperties() {
        return botProperties;
    }

    public <T> CompletableFuture<T> play(Conversation<T> conversation) {
        hydrateConversationRuntime(conversation);
        return conversation.play();
    }

    /**
     * Sends a question and remembers enough conversation state (pending interaction) to handle the user's next answer.
     * Use this when the conversation should pause here and continue/resume after
     * the user replies.
     */
    public CompletableFuture<Void> ask(
            Conversation<?> conversation,
            ConversationTopic topic,
            Question question) {

        PendingInteraction interaction = new PendingInteraction(
                conversation.getAddress(),
                conversation.getClass().getName(),
                topic.getKey(),
                question,
                conversation.getFacts()
        );

        pendingInteractionStore.save(interaction, activeScope.get(), pendingInteractionTtl);

        return outputChannelRegistry.send(OutgoingMessage.question(conversation.getAddress(), question));
    }

    /**
     * Sends a final message for the conversation and closes it without waiting for another reply.
     * Use this for terminal notes such as confirmations, receipts, or "nothing else to do" messages.
     * Any topics still playable downstream are ignored because the conversation is explicitly done.
     */
    public CompletableFuture<Void> conclude(
            Conversation<?> conversation,
            String text) {
        Objects.requireNonNull(conversation, "conversation must not be null");
        Objects.requireNonNull(text, "text must not be null");

        CompletableFuture<Void> sent = outputChannelRegistry.send(OutgoingMessage.text(conversation.getAddress(), text));
        conversation.abandon();
        return sent;
    }

    @Override
    public CompletableFuture<Void> handle(IncomingMessage message) {
        Objects.requireNonNull(message, "message must not be null");

        ConversationRoute route = conversationMessageRouter.route(message, conversationRoutingContext);
        if (route == null) {
            route = ConversationRoute.resumeOrStart(message.getAddress());
        }

        IncomingMessage routedMessage = withAddress(message, route.address());

        if (ConversationRouteMode.REJECT.equals(route.mode())) {
            return noConversationStarted(routedMessage);
        }

        if (ConversationRouteMode.START_ONLY.equals(route.mode())) {
            return startConversationOrReject(routedMessage, route.scope());
        }

        Optional<PendingInteraction> pendingInteraction = pendingInteractionStore.findByAddress(route.address(), route.scope());
        if (pendingInteraction.isPresent()) {
            return resumeConversation(routedMessage, route.scope(), pendingInteraction.get());
        }

        if (ConversationRouteMode.RESUME_ONLY.equals(route.mode())) {
            return noConversationStarted(routedMessage);
        }

        return startConversationOrReject(routedMessage, route.scope());
    }

    private CompletableFuture<Void> resumeConversation(
            IncomingMessage message,
            ConversationScope scope,
            PendingInteraction interaction) {
        Question question = interaction.getQuestion();

        Option selectedOption = null;

        if (question.isChoice()) {
            selectedOption = optionSelector.select(question, message)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid option: " + message.getText()
                    ));
        }

        Conversation<?> conversation = conversationFactory.create(
                interaction.getConversationType(),
                interaction.getAddress()
        );

        conversation.getFacts().clear();
        interaction.getFacts().forEach(conversation.getFacts()::add);
        hydrateConversationRuntime(conversation);

        ConversationTopic topic = conversation.getTopic(interaction.getCurrentTopicKey())
                .orElseThrow(() -> new IllegalStateException(
                        "Conversation does not contain topic " + interaction.getCurrentTopicKey()
                ));

        topic.onInput(new SelectedAnswer(selectedOption, message.getText(), message));

        pendingInteractionStore.deleteByAddress(message.getAddress(), scope);

        playInScope(conversation, scope);

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> startConversationOrReject(
            IncomingMessage message,
            ConversationScope scope) {
        return conversationStartRegistry.resolve(message, scope)
                .map(startRequest -> startConversation(message, scope, startRequest))
                .orElseGet(() -> noConversationStarted(message));
    }

    private CompletableFuture<Void> startConversation(
            IncomingMessage message,
            ConversationScope scope,
            ConversationStartRequest startRequest) {
        Conversation<?> conversation = conversationFactory.create(
                startRequest.getConversationType(),
                startRequest.getAddress()
        );

        startRequest.getInitialFacts().forEach(conversation.getFacts()::put);
        conversation.getFacts().put("__talkative.start.trigger", startRequest.getTrigger());
        conversation.getFacts().put("__talkative.start.raw_input", message.getText());
        hydrateConversationRuntime(conversation);

        playInScope(conversation, scope);

        return CompletableFuture.completedFuture(null);
    }

    private void playInScope(
            Conversation<?> conversation,
            ConversationScope scope
    ) {
        ConversationScope previousScope = activeScope.get();
        // Begin the synchronous routed-scope window for the current handle(...) call.
        activeScope.set(scope == null ? ConversationScope.DEFAULT : scope);
        try {
            conversation.play();
        } finally {
            // End the synchronous routed-scope window and restore the caller's scope.
            activeScope.set(previousScope);
        }
    }

    private IncomingMessage withAddress(
            IncomingMessage message,
            ConversationAddress address
    ) {
        if (message.getAddress().equals(address)) {
            return message;
        }

        return new IncomingMessage(
                message.getId(),
                address,
                message.getText(),
                message.getReceivedAt(),
                message.getEventType(),
                message.getChannel(),
                message.getExternalIdentity(),
                message.getReferral(),
                message.getMetadata(),
                message.getRawPayloadReference()
        );
    }

    private void hydrateConversationRuntime(Conversation<?> conversation) {
        Class<? extends Conversation<?>> conversationType = getConversationType(conversation);

        ConversationRuntime runtime = runtimeRegistry.getOrInitialize(
                conversationType,
                runtimeInitializerResolver.resolve(conversationType)
        );

        if (conversation instanceof RuntimeAwareConversation) {
            ((RuntimeAwareConversation) conversation).setRuntime(runtime);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Conversation<?>> getConversationType(Conversation<?> conversation) {
        return (Class<? extends Conversation<?>>) conversation.getClass();
    }

    private CompletableFuture<Void> noConversationStarted(IncomingMessage message) {
        return outputChannelRegistry.send(
                OutgoingMessage.text(
                        message.getAddress(),
                        "I don't know how to start a conversation from: " + message.getText()
                )
        );
    }

}
