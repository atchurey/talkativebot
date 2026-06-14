package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.ConversationMessageRouter;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRegistry;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartResolver;
import com.atchurey.tools.talkativebot.core.channel.DefaultConversationMessageRouter;
import com.atchurey.tools.talkativebot.core.channel.DefaultOptionSelector;
import com.atchurey.tools.talkativebot.core.channel.InputChannel;
import com.atchurey.tools.talkativebot.core.channel.OptionSelector;
import com.atchurey.tools.talkativebot.core.channel.OutputChannel;
import com.atchurey.tools.talkativebot.core.channel.OutputChannelRegistry;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import com.atchurey.tools.talkativebot.core.conversation.ConversationFactory;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializerResolver;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeRegistry;
import com.atchurey.tools.talkativebot.core.conversation.ReflectionConversationFactory;
import com.atchurey.tools.talkativebot.springbootstarter.topic.SpringTopicFactory;
import com.atchurey.tools.talkativebot.springbootstarter.topic.SpringTopicScanner;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import com.atchurey.tools.talkativebot.core.store.InMemoryPendingInteractionStore;
import com.atchurey.tools.talkativebot.springbootstarter.channels.InputChannelLifecycle;
import com.atchurey.tools.talkativebot.springbootstarter.configs.properties.SpringBootTalkativeBotProperties;
import org.springframework.beans.factory.ObjectProvider;
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
@EnableConfigurationProperties(SpringBootTalkativeBotProperties.class)
public class TalkativeBotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TalkativeBotProperties provideTalkativebotProperties(SpringBootTalkativeBotProperties springBootTalkativebotProperties) {
        try {
            // Transfer properties from Spring Boot properties to core TalkativeBotProperties
            TalkativeBotProperties talkativebotProperties = new TalkativeBotProperties();
            talkativebotProperties.setHello(springBootTalkativebotProperties.getHello());
            talkativebotProperties.setTopicBasePackage(springBootTalkativebotProperties.getTopicBasePackage());

            TalkativeBotProperties.Channels channels = new TalkativeBotProperties.Channels();
            channels.setEnabled(springBootTalkativebotProperties.getChannels().isEnabled());
            channels.setConsoleEnabled(springBootTalkativebotProperties.getChannels().isConsoleEnabled());
            ConversationAddress consoleAddress = springBootTalkativebotProperties.getChannels().getConsoleAddress() != null ?
                    springBootTalkativebotProperties.getChannels().getConsoleAddress() : new ConversationAddress("console", "console-user", "console-session");
            channels.setConsoleAddress(consoleAddress);
            talkativebotProperties.setChannels(channels);


            TalkativeBotProperties.PendingInteraction pendingInteraction = new TalkativeBotProperties.PendingInteraction();
            pendingInteraction.setStore(TalkativeBotProperties.PendingInteraction.StoreType.valueOf(springBootTalkativebotProperties.getPendingInteraction().getStore().name()));
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
    @ConditionalOnMissingBean
    public ConversationMessageRouter conversationMessageRouter() {
        return new DefaultConversationMessageRouter();
    }

    @Bean
    @ConditionalOnMissingBean(PendingInteractionStore.class)
    @ConditionalOnProperty(
            prefix = "atchurey.tools.talkativebot.pending-interaction",
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

        return new ReflectionConversationFactory(talkativeBotProvider::getObject);
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicScanner topicScanner(SpringBootTalkativeBotProperties properties) {
        return new SpringTopicScanner(properties.getTopicBasePackage());
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicFactory topicFactory(ApplicationContext applicationContext) {
        return new SpringTopicFactory(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "atchurey.tools.talkativebot.channels",
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
            prefix = "atchurey.tools.talkativebot.pending-interaction",
            name = "store",
            havingValue = "redis"
    )
    public PendingInteractionStore missingRedisPendingInteractionStoreFailure() {
        throw new IllegalStateException(
                "atchurey.tools.talkativebot.pending-interaction.store=redis, " +
                        "but Spring Data Redis is not available or no RedisOperations bean exists. " +
                        "Add spring-boot-starter-data-redis and configure RedisTemplate<String, Object>."
        );
    }

    @Bean
    @ConditionalOnMissingBean(PendingInteractionStore.class)
    @ConditionalOnProperty(
            prefix = "atchurey.tools.talkativebot.pending-interaction",
            name = "store",
            havingValue = "database"
    )
    public PendingInteractionStore missingDatabasePendingInteractionStoreFailure() {
        throw new IllegalStateException(
                "atchurey.tools.talkativebot.pending-interaction.store=database, " +
                        "but Spring Data JPA is not available or JpaPendingInteractionRepository is not registered. " +
                        "Add spring-boot-starter-data-jpa and a JDBC driver."
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TalkativeBot talkativeBot(TalkativeBotProperties properties,
                                     PendingInteractionStore pendingInteractionStore,
                                     TopicScanner topicScanner,
                                     TopicFactory topicFactory,
                                     OutputChannelRegistry outputChannelRegistry,
                                     OptionSelector optionSelector,
                                     ConversationFactory conversationFactory,
                                     ConversationStartRegistry conversationStartRegistry,
                                     ConversationRuntimeRegistry conversationRuntimeRegistry,
                                     ConversationRuntimeInitializerResolver conversationRuntimeInitializerResolver,
                                     ConversationMessageRouter conversationMessageRouter) {
        return new TalkativeBot(
                properties,
                pendingInteractionStore,
                topicScanner,
                topicFactory,
                outputChannelRegistry,
                optionSelector,
                conversationFactory,
                conversationStartRegistry,
                conversationRuntimeRegistry,
                conversationRuntimeInitializerResolver,
                conversationMessageRouter);
    }

}
