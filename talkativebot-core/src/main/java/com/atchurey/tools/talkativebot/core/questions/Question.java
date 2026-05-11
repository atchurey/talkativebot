package com.atchurey.tools.talkativebot.core.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String text;
    private final Option[] options;
    private final QuestionType type;

    public Question(String text, Option[] options) {
        this(text, options, options == null || options.length == 0 ? QuestionType.TEXT : QuestionType.CHOICE);
    }

    @JsonCreator
    public Question(
            @JsonProperty("text") String text,
            @JsonProperty("options") Option[] options,
            @JsonProperty("type") QuestionType type
    ) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.options = options == null ? new Option[0] : Arrays.copyOf(options, options.length);
        this.type = type != null
                ? type
                : this.options.length == 0 ? QuestionType.TEXT : QuestionType.CHOICE;
    }

    public static Question choice(String text, Option[] options) {
        if (options == null || options.length == 0) {
            throw new IllegalArgumentException("Choice question must have at least one option");
        }

        return new Question(text, options, QuestionType.CHOICE);
    }

    public static Question text(String text) {
        return new Question(text, new Option[0], QuestionType.TEXT);
    }

    public String getText() {
        return text;
    }

    public Option[] getOptions() {
        return Arrays.copyOf(options, options.length);
    }

    public QuestionType getType() {
        return type;
    }

    public boolean isChoice() {
        return QuestionType.CHOICE.equals(type);
    }

    public boolean isText() {
        return QuestionType.TEXT.equals(type);
    }
}