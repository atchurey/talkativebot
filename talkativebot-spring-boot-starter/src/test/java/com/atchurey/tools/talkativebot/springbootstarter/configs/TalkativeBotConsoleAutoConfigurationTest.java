package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.channel.console.ConsoleInputChannel;
import com.atchurey.tools.talkativebot.core.channel.console.ConsoleOutputChannel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TalkativeBotConsoleAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TalkativeBotAutoConfiguration.class,
                    TalkativeBotConversationRuntimeAutoConfiguration.class,
                    TalkativeBotConsoleAutoConfiguration.class
            ));

    @Test
    void consoleBeansNotCreatedByDefault() {
        this.contextRunner.run((context) -> {
            assertThat(context).doesNotHaveBean(ConsoleInputChannel.class);
            assertThat(context).doesNotHaveBean(ConsoleOutputChannel.class);
        });
    }

    @Test
    void consoleBeansCreatedWhenEnabled() {
        this.contextRunner.withPropertyValues("atchurey.tools.talkativebot.channels.console-enabled=true")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ConsoleInputChannel.class);
                    assertThat(context).hasSingleBean(ConsoleOutputChannel.class);
                });
    }
}
