package com.atchurey.talkativebot.springbootstarter.configs;

import com.atchurey.talkativebot.core.channel.console.ConsoleInputChannel;
import com.atchurey.talkativebot.core.channel.console.ConsoleOutputChannel;
import com.atchurey.talkativebot.core.configs.TalkativebotProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ConsoleInputChannel.class)
@ConditionalOnProperty(
        prefix = "atchurey.talkative.bot.channels",
        name = "console-enabled",
        havingValue = "true"
)
public class TalkativeBotConsoleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsoleInputChannel consoleInputChannel(TalkativebotProperties properties) {
        return new ConsoleInputChannel(properties.getChannels().getConsoleAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    ConsoleOutputChannel consoleOutputChannel() {
        return new ConsoleOutputChannel();
    }
}
