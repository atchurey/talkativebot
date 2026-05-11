package com.atchurey.tools.talkativebot.springbootstarter.configs.properties;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Setter
@Getter
@ConfigurationProperties("atchurey.tools.talkativebot")
@Validated
public class SpringBootTalkativebotProperties {
    private String hello;
    @NotNull
    private String topicBasePackage;
    private Channels channels = new Channels();
    private PendingInteraction pendingInteraction = new PendingInteraction();

    @Setter
    @Getter
    public static class Channels {
        private boolean enabled = true;
        private boolean consoleEnabled = false;
        private ConversationAddress consoleAddress = new ConversationAddress("console", "console-user", "console-session", "console-conversation-1"
        );;
    }

    @Setter
    @Getter
    public static class PendingInteraction {
        private StoreType store = StoreType.MEMORY;
        private Duration ttl = Duration.ofMinutes(30);

        public enum StoreType {
            MEMORY,
            REDIS,
            DATABASE
        }
    }

}
