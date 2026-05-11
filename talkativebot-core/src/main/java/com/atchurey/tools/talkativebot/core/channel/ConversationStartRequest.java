package com.atchurey.tools.talkativebot.core.channel;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Getter
public class ConversationStartRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ConversationAddress address;
    private final String conversationType;
    private final String trigger;
    private final Map<String, Serializable> initialFacts;

    public ConversationStartRequest(
            ConversationAddress address,
            String conversationType,
            String trigger,
            Map<String, Serializable> initialFacts
    ) {
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.conversationType = Objects.requireNonNull(conversationType, "conversationType must not be null");
        this.trigger = trigger;
        this.initialFacts = initialFacts == null ? Collections.emptyMap() : Map.copyOf(initialFacts);
    }

}
