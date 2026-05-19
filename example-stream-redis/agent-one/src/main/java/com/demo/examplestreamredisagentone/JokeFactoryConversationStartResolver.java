package com.demo.examplestreamredisagentone;

import com.atchurey.tools.talkativebot.core.channel.ConversationStartRequest;
import com.atchurey.tools.talkativebot.core.channel.ConversationStartResolver;
import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Component
public class JokeFactoryConversationStartResolver implements ConversationStartResolver {

    @Override
    public Optional<ConversationStartRequest> resolve(IncomingMessage message) {
        if (!"/start".equalsIgnoreCase(message.getText().trim())) {
            return Optional.empty();
        }

        return Optional.of(new ConversationStartRequest(
                message.getAddress(),
                JokeFactoryConversation.class.getName(),
                "/start",
                Map.of() // No initial facts required for this conversation
        ));
    }

    private Serializable valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}