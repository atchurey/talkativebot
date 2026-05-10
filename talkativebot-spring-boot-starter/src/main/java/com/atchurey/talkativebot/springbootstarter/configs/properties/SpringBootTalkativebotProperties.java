package com.atchurey.talkativebot.springbootstarter.configs.properties;

import com.atchurey.talkativebot.core.channel.ConversationAddress;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Setter
@Getter
@ConfigurationProperties("atchurey.talkativebot")
public class SpringBootTalkativebotProperties {
    private String hello;
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
