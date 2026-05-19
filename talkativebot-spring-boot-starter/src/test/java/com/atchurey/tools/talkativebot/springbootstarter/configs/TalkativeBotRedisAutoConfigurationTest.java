package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.JpaPendingInteractionRepository;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.JpaPendingInteractionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TalkativeBotRedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TalkativeBotAutoConfiguration.class,
                    TalkativeBotJpaAutoConfiguration.class,
                    TalkativeBotConversationRuntimeAutoConfiguration.class
            ));

    @Test
    void jpaStoreCreatedWhenPropertySetAndJpaAvailable() {
        this.contextRunner
                .withUserConfiguration(JpaMockConfiguration.class)
                .withPropertyValues("atchurey.tools.talkativebot.pending-interaction.store=database")
                .run((context) -> {
                    assertThat(context).hasSingleBean(PendingInteractionStore.class);
                });
    }

    @Test
    void jpaStoreNotCreatedWhenPropertyNotSet() {
        this.contextRunner
                .withUserConfiguration(JpaMockConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(PendingInteractionStore.class);
                    assertThat(context).getBean(PendingInteractionStore.class).isNotInstanceOf(JpaPendingInteractionStore.class);
                });
    }

    @Configuration
    static class JpaMockConfiguration {
        @Bean
        public JpaPendingInteractionRepository jpaPendingInteractionRepository() {
            return mock(JpaPendingInteractionRepository.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
