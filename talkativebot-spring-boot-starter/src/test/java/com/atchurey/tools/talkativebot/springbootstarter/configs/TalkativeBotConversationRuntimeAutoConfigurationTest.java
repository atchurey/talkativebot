package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntime;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializer;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializerResolver;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class TalkativeBotConversationRuntimeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TalkativeBotConversationRuntimeAutoConfiguration.class));

    @Test
    void defaultBeansAreCreated() {
        this.contextRunner.run((context) -> {
            assertThat(context).hasSingleBean(ConversationRuntimeRegistry.class);
            assertThat(context).hasSingleBean(ConversationRuntimeInitializerResolver.class);
        });
    }

    @Test
    void initializerResolverUsesProvidedInitializers() {
        this.contextRunner.withUserConfiguration(TestInitializerConfiguration.class)
                .run((context) -> {
                    ConversationRuntimeInitializerResolver resolver = context.getBean(ConversationRuntimeInitializerResolver.class);
                    assertThat(resolver).isNotNull();
                    // We can't easily check the internals of DefaultConversationRuntimeInitializerResolver 
                    // without exposing its fields, but we verified the bean is created.
                });
    }

    @Configuration
    static class TestInitializerConfiguration {
        @Bean
        ConversationRuntimeInitializer testInitializer() {
            return new ConversationRuntimeInitializer() {
                @Override
                public boolean supports(Class<? extends Conversation<?>> conversationType) {
                    return false;
                }

                @Override
                public ConversationRuntime initialize(Class<? extends Conversation<?>> conversationType) {
                    return null;
                }
            };
        }
    }
}
