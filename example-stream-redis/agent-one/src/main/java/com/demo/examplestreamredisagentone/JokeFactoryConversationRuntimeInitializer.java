package com.demo.examplestreamredisagentone;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntime;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializer;
import com.atchurey.tools.talkativebot.core.conversation.DefaultConversationRuntime;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JokeFactoryConversationRuntimeInitializer
        implements ConversationRuntimeInitializer {

    private final WebClient.Builder webClientBuilder;

    public JokeFactoryConversationRuntimeInitializer(
            WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public boolean supports(Class<? extends Conversation<?>> conversationType) {
        return JokeFactoryConversation.class.equals(conversationType);
    }

    @Override
    public ConversationRuntime initialize(Class<? extends Conversation<?>> conversationType) {
        WebClient webClient = webClientBuilder
                .baseUrl("https://official-joke-api.appspot.com")
                .build();

        JokeFactoryRuntime runtime = new JokeFactoryRuntime(webClient);

        return DefaultConversationRuntime.builder()
                .put(JokeFactoryRuntimeKeys.RUNTIME, runtime)
                .put(JokeFactoryRuntimeKeys.JOKE_WEB_CLIENT, webClient)
                .build();
    }
}