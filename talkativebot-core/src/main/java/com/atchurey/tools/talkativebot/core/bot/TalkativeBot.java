package com.atchurey.tools.talkativebot.core.bot;

import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRegistry;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRequest;
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
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import com.atchurey.tools.talkativebot.core.topic.ConversationTopic;
import com.atchurey.tools.talkativebot.core.topic.ReflectionTopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import lombok.Getter;

import java.time.Duration;
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

    public TalkativeBot(
            TalkativeBotProperties botProperties,
            PendingInteractionStore pendingInteractionStore,
            TopicScanner topicScanner,
            TopicFactory topicFactory,
            OutputChannelRegistry outputChannelRegistry,
            OptionSelector optionSelector,
            ConversationFactory conversationFactory,
            ConversationStartRegistry conversationStartRegistry) {

        this.botProperties = botProperties;
        this.pendingInteractionStore = pendingInteractionStore;
        this.topicScanner = topicScanner;
        this.topicFactory = topicFactory == null ? new ReflectionTopicFactory() : topicFactory;
        this.outputChannelRegistry = outputChannelRegistry;
        this.optionSelector = optionSelector;
        this.conversationFactory = conversationFactory;
        this.pendingInteractionTtl = botProperties.getPendingInteraction().getTtl();
        this.conversationStartRegistry = conversationStartRegistry;
    }

    public TalkativeBotProperties getBotConfigProperties() {
        return botProperties;
    }

    public <T> CompletableFuture<T> play(Conversation<T> conversation) {
        return conversation.play();
    }

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

        pendingInteractionStore.save(interaction, pendingInteractionTtl);

        return outputChannelRegistry.send(OutgoingMessage.question(conversation.getAddress(), question));
    }

    @Override
    public CompletableFuture<Void> handle(IncomingMessage message) {
        return pendingInteractionStore.findByAddress(message.getAddress())
                .map(interaction -> resumeConversation(message, interaction))
                .orElseGet(() -> startConversationOrReject(message));
    }

    private CompletableFuture<Void> resumeConversation(
            IncomingMessage message,
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

        ConversationTopic topic = conversation.getTopic(interaction.getCurrentTopicKey())
                .orElseThrow(() -> new IllegalStateException(
                        "Conversation does not contain topic " + interaction.getCurrentTopicKey()
                ));

        topic.onInput(new SelectedAnswer(selectedOption, message.getText(), message));

        pendingInteractionStore.deleteByAddress(message.getAddress());

        conversation.play();

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> startConversationOrReject(IncomingMessage message) {
        return conversationStartRegistry.resolve(message)
                .map(startRequest -> startConversation(message, startRequest))
                .orElseGet(() -> noConversationStarted(message));
    }

    private CompletableFuture<Void> startConversation(
            IncomingMessage message,
            ConversationStartRequest startRequest) {
        Conversation<?> conversation = conversationFactory.create(
                startRequest.getConversationType(),
                startRequest.getAddress()
        );

        startRequest.getInitialFacts().forEach(conversation.getFacts()::put);

        conversation.getFacts().put("__talkative.start.trigger", startRequest.getTrigger());
        conversation.getFacts().put("__talkative.start.raw_input", message.getText());

        conversation.play();

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> noConversationStarted(IncomingMessage message) {
        return outputChannelRegistry.send(
                OutgoingMessage.text(
                        message.getAddress(),
                        "I don't know how to start a conversation from: " + message.getText()
                )
        );
    }

    public CompletableFuture<String> reply(String text) {
        return CompletableFuture.completedFuture(text);
    }
}