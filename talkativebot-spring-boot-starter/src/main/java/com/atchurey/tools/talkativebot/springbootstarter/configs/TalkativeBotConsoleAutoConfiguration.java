package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.channel.console.ConsoleInputChannel;
import com.atchurey.tools.talkativebot.core.channel.console.ConsoleOutputChannel;
import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ConsoleInputChannel.class)
@ConditionalOnProperty(
        prefix = "atchurey.tools.talkativebot.channels",
        name = "console-enabled",
        havingValue = "true"
)
public class TalkativeBotConsoleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsoleInputChannel consoleInputChannel(TalkativeBotProperties properties) {
        return new ConsoleInputChannel(properties.getChannels().getConsoleAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    ConsoleOutputChannel consoleOutputChannel() {
        return new ConsoleOutputChannel();
    }
}
