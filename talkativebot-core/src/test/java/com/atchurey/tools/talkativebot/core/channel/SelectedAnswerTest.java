package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Option;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectedAnswerTest {

    @Test
    void shouldCreateAnswerWithOption() {
        Option option = mock(Option.class);
        when(option.getValue()).thenReturn(1);
        when(option.getText()).thenReturn("Yes");
        IncomingMessage message = mock(IncomingMessage.class);

        SelectedAnswer answer = new SelectedAnswer(option, "1", message);

        assertThat(answer.getOption()).isPresent().contains(option);
        assertThat(answer.getRawInput()).isEqualTo("1");
        assertThat(answer.getMessage()).isEqualTo(message);
        assertThat(answer.hasOption()).isTrue();
        assertThat(answer.getValue()).isEqualTo(1);
        assertThat(answer.getText()).isEqualTo("Yes");
    }

    @Test
    void shouldCreateAnswerWithoutOption() {
        IncomingMessage message = mock(IncomingMessage.class);
        SelectedAnswer answer = new SelectedAnswer(null, "some text", message);

        assertThat(answer.getOption()).isEmpty();
        assertThat(answer.getRawInput()).isEqualTo("some text");
        assertThat(answer.getMessage()).isEqualTo(message);
        assertThat(answer.hasOption()).isFalse();
        assertThat(answer.getText()).isEqualTo("some text");
    }

    @Test
    void shouldThrowWhenGettingValueWithoutOption() {
        SelectedAnswer answer = new SelectedAnswer(null, "text", mock(IncomingMessage.class));

        assertThatThrownBy(answer::getValue)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Selected answer has no option");
    }
}
