package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;

import java.util.Arrays;
import java.util.Optional;

/**
 * A default implementation of the {@link OptionSelector} interface.
 * This class provides functionality for selecting an {@link Option}
 * based on user input from an {@link IncomingMessage}. The selection 
 * is made using either the numerical value of the option or its text.
 *
 * The selection logic is as follows:
 * 1. Attempts to parse the input text as an integer and match it 
 *    against the value of the options in the given {@link Question}.
 * 2. If value finds no match, attempts to match the input 
 *    text against the text of the options, ignoring case sensitivity.
 */
public class DefaultOptionSelector implements OptionSelector {

    @Override
    public Optional<Option> select(Question question, IncomingMessage message) {
        String input = message.getText().trim();

        Optional<Option> byValue = trySelectByValue(question, input);

        if (byValue.isPresent()) {
            return byValue;
        }

        return Arrays.stream(question.getOptions())
                .filter(option -> option.getText().equalsIgnoreCase(input))
                .findFirst();
    }

    private Optional<Option> trySelectByValue(Question question, String input) {
        try {
            int selectedValue = Integer.parseInt(input);

            return Arrays.stream(question.getOptions())
                    .filter(option -> option.getValue() == selectedValue)
                    .findFirst();
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
