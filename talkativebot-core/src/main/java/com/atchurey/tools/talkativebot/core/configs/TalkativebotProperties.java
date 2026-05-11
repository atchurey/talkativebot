package com.atchurey.tools.talkativebot.core.configs;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Data
public class TalkativebotProperties {
    private String hello;
    private String topicBasePackage;
    private Channels channels = new Channels();
    private PendingInteraction pendingInteraction = new PendingInteraction();

    @Setter
    @Getter
    public static class Channels {
        private boolean enabled = true;
        private boolean consoleEnabled = false;
        private ConversationAddress consoleAddress = new ConversationAddress("console", "console-user", "console-session", "console-conversation-1");
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
