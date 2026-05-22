package com.atchurey.tools.talkativebot.core.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int value;
    private final String text;

    @JsonCreator
    public Option(
            @JsonProperty("value") int value,
            @JsonProperty("text") String text) {
        this.value = value;
        this.text = text;
    }

}