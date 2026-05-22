package com.atchurey.tools.talkativebot.core.conversation;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.topic.TopicFactory;
import com.atchurey.tools.talkativebot.core.topic.TopicScanner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReflectionConversationFactoryTest {

    private final TalkativeBot mockBot = mock(TalkativeBot.class);
    private final ReflectionConversationFactory factory = new ReflectionConversationFactory(() -> mockBot);

    @Test
    void createSuccess() {
        TopicScanner mockScanner = mock(TopicScanner.class);
        TopicFactory mockTopicFactory = mock(TopicFactory.class);
        when(mockBot.getTopicScanner()).thenReturn(mockScanner);
        when(mockBot.getTopicFactory()).thenReturn(mockTopicFactory);

        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        Conversation<?> conversation = factory.create(TestConversation.class.getName(), address);

        assertThat(conversation).isInstanceOf(TestConversation.class);
        assertThat(((TestConversation) conversation).getBot()).isEqualTo(mockBot);
        assertThat(conversation.getAddress()).isEqualTo(address);
    }

    @Test
    void createFailureInvalidClass() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        assertThatThrownBy(() -> factory.create("non.existent.Class", address))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Could not create conversation");
    }

    @Test
    void createFailureNotAConversation() {
        ConversationAddress address = new ConversationAddress("c", "u", "s", "conv");
        assertThatThrownBy(() -> factory.create(String.class.getName(), address))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not implement Conversation");
    }

    public static class TestConversation extends AbstractConversation<String> {
        public TestConversation(TalkativeBot bot, ConversationAddress address) {
            super(bot, address);
        }

        public TalkativeBot getBot() {
            return bot;
        }

        @Override
        public java.util.concurrent.CompletableFuture<String> play() {
            return null;
        }
    }
}
