package com.atchurey.tools.talkativebot.springbootstarter;

import com.atchurey.tools.talkativebot.core.bot.Talkativebot;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartRegistry;
import com.atchurey.tools.talkativebot.core.channel.OptionSelector;
import com.atchurey.tools.talkativebot.core.channel.OutputChannelRegistry;
import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.core.configs.TalkativebotProperties;
import com.atchurey.tools.talkativebot.core.conversation.ConversationFactory;
import com.atchurey.tools.talkativebot.core.store.InMemoryPendingInteractionStore;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import com.atchurey.tools.talkativebot.springbootstarter.configs.TalkativebotAutoConfiguration;
import com.atchurey.tools.talkativebot.springbootstarter.configs.properties.SpringBootTalkativebotProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TalkativebotAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TalkativebotAutoConfiguration.class))
            .withPropertyValues(
                    "atchurey.tools.talkativebot.hello=hello test bot!",
                    "atchurey.tools.talkativebot.topic-base-package=com.example.bot",
                    "atchurey.tools.talkativebot.pending-interaction.store=memory",
                    "atchurey.tools.talkativebot.pending-interaction.ttl=30m",
                    "atchurey.tools.talkativebot.channels.enabled=true",
                    "atchurey.tools.talkativebot.channels.console-enabled=false"
            );

    @Test
    void shouldAutoConfigureTalkativebotSuccessfully() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            assertThat(context).hasSingleBean(SpringBootTalkativebotProperties.class);
            assertThat(context).hasSingleBean(TalkativebotProperties.class);
            assertThat(context).hasSingleBean(PendingInteractionStore.class);
            assertThat(context).hasSingleBean(OptionSelector.class);
            assertThat(context).hasSingleBean(ConversationStartRegistry.class);
            assertThat(context).hasSingleBean(OutputChannelRegistry.class);
            assertThat(context).hasSingleBean(ConversationFactory.class);
            assertThat(context).hasSingleBean(TopicScanner.class);
            assertThat(context).hasSingleBean(TopicFactory.class);
            assertThat(context).hasSingleBean(Talkativebot.class);

            assertThat(context.getBean(PendingInteractionStore.class))
                    .isInstanceOf(InMemoryPendingInteractionStore.class);

            TalkativebotProperties properties = context.getBean(TalkativebotProperties.class);

            assertThat(properties.getHello()).isEqualTo("hello test bot!");
            assertThat(properties.getTopicBasePackage()).isEqualTo("com.example.bot");
            assertThat(properties.getPendingInteraction().getStore())
                    .isEqualTo(TalkativebotProperties.PendingInteraction.StoreType.MEMORY);
            assertThat(properties.getPendingInteraction().getTtl())
                    .isEqualTo(Duration.ofMinutes(30));
            assertThat(properties.getChannels().isEnabled()).isTrue();
            assertThat(properties.getChannels().isConsoleEnabled()).isFalse();

            Talkativebot talkativebot = context.getBean(Talkativebot.class);

            assertThat(talkativebot).isNotNull();
            assertThat(talkativebot.getBotConfigProperties()).isSameAs(properties);
        });
    }
}
