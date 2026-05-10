package com.atchurey.talkativebot.springbootstarter.configs;

import com.atchurey.talkativebot.core.bot.TalkativeBot;
import com.atchurey.talkativebot.core.channel.ConversationAddress;
import com.atchurey.talkativebot.core.channel.ConversationStartRegistry;
import com.atchurey.talkativebot.core.channel.ConversationStartResolver;
import com.atchurey.talkativebot.core.channel.DefaultOptionSelector;
import com.atchurey.talkativebot.core.channel.InputChannel;
import com.atchurey.talkativebot.core.channel.OptionSelector;
import com.atchurey.talkativebot.core.channel.OutputChannel;
import com.atchurey.talkativebot.core.channel.OutputChannelRegistry;
import com.atchurey.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.talkativebot.core.configs.TalkativebotProperties;
import com.atchurey.talkativebot.core.conversation.ConversationFactory;
import com.atchurey.talkativebot.core.conversation.ReflectionConversationFactory;
import com.atchurey.talkativebot.springbootstarter.topic.SpringTopicFactory;
import com.atchurey.talkativebot.springbootstarter.topic.SpringTopicScanner;
import com.atchurey.talkativebot.core.topic.TopicFactory;
import com.atchurey.talkativebot.core.topic.TopicScanner;
import com.atchurey.talkativebot.core.store.InMemoryPendingInteractionStore;
import com.atchurey.talkativebot.springbootstarter.channels.InputChannelLifecycle;
import com.atchurey.talkativebot.springbootstarter.configs.properties.SpringBootTalkativebotProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(TalkativeBot.class)
@EnableConfigurationProperties(SpringBootTalkativebotProperties.class)
public class TalkativeBotAutoConfiguration {

    @Autowired
    SpringBootTalkativebotProperties springBootTalkativebotProperties;

    @Bean
    @ConditionalOnMissingBean
    public TalkativebotProperties provideTalkativebotProperties() {
        try {
            // Transfer properties from Spring Boot properties to core TalkativebotProperties
            TalkativebotProperties talkativebotProperties = new TalkativebotProperties();
            talkativebotProperties.setHello(springBootTalkativebotProperties.getHello());
            talkativebotProperties.setTopicBasePackage(springBootTalkativebotProperties.getTopicBasePackage());

            TalkativebotProperties.Channels channels = new TalkativebotProperties.Channels();
            channels.setEnabled(springBootTalkativebotProperties.getChannels().isEnabled());
            channels.setConsoleEnabled(springBootTalkativebotProperties.getChannels().isConsoleEnabled());
            ConversationAddress consoleAddress = springBootTalkativebotProperties.getChannels().getConsoleAddress() != null ?
                    springBootTalkativebotProperties.getChannels().getConsoleAddress() : new ConversationAddress("console", "console-user", "console-session", "console-conversation-1");
            channels.setConsoleAddress(consoleAddress);
            talkativebotProperties.setChannels(channels);


            TalkativebotProperties.PendingInteraction pendingInteraction = new TalkativebotProperties.PendingInteraction();
            pendingInteraction.setStore(TalkativebotProperties.PendingInteraction.StoreType.valueOf(springBootTalkativebotProperties.getPendingInteraction().getStore().name()));
            pendingInteraction.setTtl(springBootTalkativebotProperties.getPendingInteraction().getTtl());
            talkativebotProperties.setPendingInteraction(pendingInteraction);

            return talkativebotProperties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public OptionSelector optionSelector() {
        return new DefaultOptionSelector();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationStartRegistry conversationStartRegistry(
            List<ConversationStartResolver> resolvers) {
        return new ConversationStartRegistry(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutputChannelRegistry outputChannelRegistry(List<OutputChannel> outputChannels) {
        return new OutputChannelRegistry(outputChannels);
    }

    @Bean
    @ConditionalOnMissingBean(PendingInteractionStore.class)
    @ConditionalOnProperty(
            prefix = "atchurey.talkative.bot.pending-interaction",
            name = "store",
            havingValue = "memory",
            matchIfMissing = true
    )
    public PendingInteractionStore pendingInteractionStore() {
        return new InMemoryPendingInteractionStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationFactory conversationFactory(
            ObjectProvider<TalkativeBot> talkativeBotProvider) {

        return new ReflectionConversationFactory(talkativeBotProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicScanner topicScanner(SpringBootTalkativebotProperties properties) {
        return new SpringTopicScanner(properties.getTopicBasePackage());
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicFactory topicFactory(ApplicationContext applicationContext) {
        return new SpringTopicFactory(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "talkativeInputChannelRunner")
    public ApplicationRunner talkativeInputChannelRunner(
            TalkativeBot talkativeBot,
            List<InputChannel> inputChannels) {
        return args -> inputChannels.forEach(inputChannel -> inputChannel.start(talkativeBot));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "atchurey.talkative.bot.channels",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public InputChannelLifecycle inputChannelLifecycle(
            TalkativeBot talkativeBot,
            List<InputChannel> inputChannels) {
        return new InputChannelLifecycle(talkativeBot, inputChannels);
    }

    @Bean
    @ConditionalOnMissingBean(PendingInteractionStore.class)
    @ConditionalOnProperty(
            prefix = "atchurey.talkative.bot.pending-interaction",
            name = "store",
            havingValue = "redis"
    )
    public PendingInteractionStore missingRedisPendingInteractionStoreFailure() {
        throw new IllegalStateException(
                "atchurey.talkative.bot.pending-interaction.store=redis, " +
                        "but Spring Data Redis is not available or no RedisOperations bean exists. " +
                        "Add spring-boot-starter-data-redis and configure RedisTemplate<String, Object>."
        );
    }

    @Bean
    @ConditionalOnMissingBean(PendingInteractionStore.class)
    @ConditionalOnProperty(
            prefix = "atchurey.talkative.bot.pending-interaction",
            name = "store",
            havingValue = "database"
    )
    public PendingInteractionStore missingDatabasePendingInteractionStoreFailure() {
        throw new IllegalStateException(
                "atchurey.talkative.bot.pending-interaction.store=database, " +
                        "but Spring Data JPA is not available or JpaPendingInteractionRepository is not registered. " +
                        "Add spring-boot-starter-data-jpa and a JDBC driver."
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TalkativeBot talkativeBot(TalkativebotProperties properties,
                                     PendingInteractionStore pendingInteractionStore,
                                     TopicScanner topicScanner,
                                     TopicFactory topicFactory,
                                     OutputChannelRegistry outputChannelRegistry,
                                     OptionSelector optionSelector,
                                     ConversationFactory conversationFactory,
                                     ConversationStartRegistry conversationStartRegistry) {
        return new TalkativeBot(
                properties,
                pendingInteractionStore,
                topicScanner,
                topicFactory,
                outputChannelRegistry,
                optionSelector,
                conversationFactory,
                conversationStartRegistry);
    }

}