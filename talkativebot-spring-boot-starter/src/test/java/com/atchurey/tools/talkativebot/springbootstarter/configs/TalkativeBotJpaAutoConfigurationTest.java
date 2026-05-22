package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.channel.PendingInteractionStore;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.JpaPendingInteractionRepository;
import com.atchurey.tools.talkativebot.springbootstarter.store.jpa.JpaPendingInteractionStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TalkativeBotJpaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TalkativeBotJpaAutoConfiguration.class
            ));

    @Test
    void jpaStoreIsNotCreatedByDefault() {
        this.contextRunner.run((context) -> {
            assertThat(context).doesNotHaveBean(JpaPendingInteractionStore.class);
            assertThat(context).doesNotHaveBean(JpaPendingInteractionRepository.class);
        });
    }

    @Test
    void jpaStoreIsNotCreatedWhenPropertyIsNotDatabase() {
        this.contextRunner.withPropertyValues("atchurey.tools.talkativebot.pending-interaction.store=memory")
                .run((context) -> {
                    assertThat(context).doesNotHaveBean(JpaPendingInteractionStore.class);
                    assertThat(context).doesNotHaveBean(JpaPendingInteractionRepository.class);
                });
    }
}
