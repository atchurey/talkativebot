package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultOptionSelectorTest {

    private final DefaultOptionSelector selector = new DefaultOptionSelector();

    @Test
    void selectByValue() {
        Question question = mock(Question.class);
        Option opt1 = new Option(1, "One");
        Option opt2 = new Option(2, "Two");
        when(question.getOptions()).thenReturn(new Option[]{opt1, opt2});

        IncomingMessage message = mock(IncomingMessage.class);
        when(message.getText()).thenReturn("2");

        Optional<Option> result = selector.select(question, message);

        assertThat(result).isPresent().contains(opt2);
    }

    @Test
    void selectByTextCaseInsensitive() {
        Question question = mock(Question.class);
        Option opt1 = new Option(1, "Yes");
        Option opt2 = new Option(2, "No");
        when(question.getOptions()).thenReturn(new Option[]{opt1, opt2});

        IncomingMessage message = mock(IncomingMessage.class);
        when(message.getText()).thenReturn("yes");

        Optional<Option> result = selector.select(question, message);

        assertThat(result).isPresent().contains(opt1);
    }

    @Test
    void selectWithWhitespace() {
        Question question = mock(Question.class);
        Option opt1 = new Option(1, "Yes");
        when(question.getOptions()).thenReturn(new Option[]{opt1});

        IncomingMessage message = mock(IncomingMessage.class);
        when(message.getText()).thenReturn("  Yes  ");

        Optional<Option> result = selector.select(question, message);

        assertThat(result).isPresent().contains(opt1);
    }

    @Test
    void noMatchReturnsEmpty() {
        Question question = mock(Question.class);
        Option opt1 = new Option(1, "Yes");
        when(question.getOptions()).thenReturn(new Option[]{opt1});

        IncomingMessage message = mock(IncomingMessage.class);
        when(message.getText()).thenReturn("Maybe");

        Optional<Option> result = selector.select(question, message);

        assertThat(result).isEmpty();
    }
}
