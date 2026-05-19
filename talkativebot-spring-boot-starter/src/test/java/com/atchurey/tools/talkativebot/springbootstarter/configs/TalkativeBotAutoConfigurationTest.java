package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRegistry;
import com.atchurey.tools.talkativebot.core.channel.OptionSelector;
import com.atchurey.tools.talkativebot.core.channel.OutputChannelRegistry;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import com.atchurey.tools.talkativebot.core.conversation.ConversationFactory;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializerResolver;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeRegistry;
import com.atchurey.tools.talkativebot.core.store.InMemoryPendingInteractionStore;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TalkativeBotAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TalkativeBotAutoConfiguration.class,
                    TalkativeBotConversationRuntimeAutoConfiguration.class
            ));

    @Test
    void defaultBeansAreCreated() {
        this.contextRunner.run((context) -> {
            assertThat(context).hasSingleBean(TalkativeBot.class);
            assertThat(context).hasSingleBean(TalkativeBotProperties.class);
            assertThat(context).hasSingleBean(OptionSelector.class);
            assertThat(context).hasSingleBean(ConversationStartRegistry.class);
            assertThat(context).hasSingleBean(OutputChannelRegistry.class);
            assertThat(context).hasSingleBean(PendingInteractionStore.class);
            assertThat(context).getBean(PendingInteractionStore.class).isInstanceOf(InMemoryPendingInteractionStore.class);
            assertThat(context).hasSingleBean(ConversationFactory.class);
            assertThat(context).hasSingleBean(TopicScanner.class);
            assertThat(context).hasSingleBean(TopicFactory.class);
        });
    }

    @Test
    void propertiesAreMappedCorrectly() {
        this.contextRunner.withPropertyValues(
                "atchurey.tools.talkativebot.hello=world",
                "atchurey.tools.talkativebot.topic-base-package=com.test",
                "atchurey.tools.talkativebot.pending-interaction.ttl=1h"
        ).run((context) -> {
            TalkativeBotProperties properties = context.getBean(TalkativeBotProperties.class);
            assertThat(properties.getHello()).isEqualTo("world");
            assertThat(properties.getTopicBasePackage()).isEqualTo("com.test");
            assertThat(properties.getPendingInteraction().getTtl().toHours()).isEqualTo(1);
        });
    }

    @Test
    void memoryStoreIsDefault() {
        this.contextRunner.run((context) -> {
            assertThat(context).getBean(PendingInteractionStore.class).isInstanceOf(InMemoryPendingInteractionStore.class);
        });
    }

    @Test
    void failsWhenRedisRequestedButMissing() {
        this.contextRunner.withPropertyValues("atchurey.tools.talkativebot.pending-interaction.store=redis")
                .run((context) -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().hasMessageContaining("atchurey.tools.talkativebot.pending-interaction.store=redis");
                });
    }

    @Test
    void failsWhenDatabaseRequestedButMissing() {
        this.contextRunner.withPropertyValues("atchurey.tools.talkativebot.pending-interaction.store=database")
                .run((context) -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().hasMessageContaining("atchurey.tools.talkativebot.pending-interaction.store=database");
                });
    }
}
