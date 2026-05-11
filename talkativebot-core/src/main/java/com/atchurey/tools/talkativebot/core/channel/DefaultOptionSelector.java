package com.atchurey.tools.talkativebot.core.channel;

import com.atchurey.tools.talkativebot.core.questions.Option;
import com.atchurey.tools.talkativebot.core.questions.Question;

import java.util.Arrays;
import java.util.Optional;

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
