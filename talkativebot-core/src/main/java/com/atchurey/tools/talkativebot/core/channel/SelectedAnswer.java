package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Option;

import java.io.Serializable;
import java.util.Optional;

public class SelectedAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Option option;
    private final String rawInput;
    private final IncomingMessage message;

    public SelectedAnswer(
            Option option,
            String rawInput,
            IncomingMessage message
    ) {
        this.option = option;
        this.rawInput = rawInput;
        this.message = message;
    }

    public Optional<Option> getOption() {
        return Optional.ofNullable(option);
    }

    public String getRawInput() {
        return rawInput;
    }

    public IncomingMessage getMessage() {
        return message;
    }

    public boolean hasOption() {
        return option != null;
    }

    public int getValue() {
        if (option == null) {
            throw new IllegalStateException("Selected answer has no option");
        }

        return option.getValue();
    }

    public String getText() {
        return option == null ? rawInput : option.getText();
    }
}
