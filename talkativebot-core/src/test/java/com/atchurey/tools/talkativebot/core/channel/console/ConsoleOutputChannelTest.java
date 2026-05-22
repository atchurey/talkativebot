package com.atchurey.tools.talkativebot.core.channel.console;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.OutgoingMessage;
import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleOutputChannelTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final ConsoleOutputChannel channel = new ConsoleOutputChannel();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void shouldSupportConsoleChannel() {
        assertThat(channel.supports(new ConversationAddress("console", "user", null, null))).isTrue();
        assertThat(channel.supports(new ConversationAddress("web", "user", null, null))).isFalse();
    }

    @Test
    void shouldSendTextMessage() {
        ConversationAddress address = new ConversationAddress("console", "user", null, null);
        OutgoingMessage message = OutgoingMessage.text(address, "Hello World");

        channel.send(message).join();

        assertThat(outputStreamCaptor.toString().trim()).isEqualTo("Hello World");
    }

    @Test
    void shouldSendQuestionMessage() {
        ConversationAddress address = new ConversationAddress("console", "user", null, null);
        Question question = mock(Question.class);
        when(question.getText()).thenReturn("What is your name?");
        when(question.isChoice()).thenReturn(false);
        OutgoingMessage message = OutgoingMessage.question(address, question);

        channel.send(message).join();

        assertThat(outputStreamCaptor.toString().trim()).isEqualTo("What is your name?");
    }

    @Test
    void shouldSendQuestionMessageWithOptions() {
        ConversationAddress address = new ConversationAddress("console", "user", null, null);
        Question question = mock(Question.class);
        when(question.getText()).thenReturn("Pick one:");
        when(question.isChoice()).thenReturn(true);
        Option opt1 = new Option(1, "One");
        Option opt2 = new Option(2, "Two");
        when(question.getOptions()).thenReturn(new Option[]{opt1, opt2});
        OutgoingMessage message = OutgoingMessage.question(address, question);

        channel.send(message).join();

        String output = outputStreamCaptor.toString();
        assertThat(output).contains("Pick one:");
        assertThat(output).contains("1. One");
        assertThat(output).contains("2. Two");
    }
}
