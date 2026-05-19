package com.atchurey.tools.talkativebot.core.channel.console;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import com.atchurey.tools.talkativebot.core.channel.InputMessageHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsoleInputChannelTest {

    private final InputStream standardIn = System.in;
    private final ConversationAddress address = new ConversationAddress("console", "user", null, null);
    private ConsoleInputChannel channel;

    @BeforeEach
    void setUp() {
        channel = new ConsoleInputChannel(address);
    }

    @AfterEach
    void tearDown() {
        System.setIn(standardIn);
        if (channel != null) {
            channel.stop();
        }
    }

    @Test
    void shouldHandleInput() throws Exception {
        String input = "Hello from console\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        InputMessageHandler handler = mock(InputMessageHandler.class);
        ArgumentCaptor<IncomingMessage> messageCaptor = ArgumentCaptor.forClass(IncomingMessage.class);
        
        CompletableFuture<Void> handleFuture = new CompletableFuture<>();
        when(handler.handle(any(IncomingMessage.class))).thenAnswer(invocation -> {
            handleFuture.complete(null);
            return CompletableFuture.completedFuture(null);
        });

        channel.start(handler);

        handleFuture.get(5, TimeUnit.SECONDS);

        verify(handler).handle(messageCaptor.capture());
        IncomingMessage message = messageCaptor.getValue();
        assertThat(message.getText()).isEqualTo("Hello from console");
        assertThat(message.getAddress()).isEqualTo(address);
    }

    @Test
    void shouldHaveName() {
        assertThat(channel.name()).isEqualTo("console");
    }
}
