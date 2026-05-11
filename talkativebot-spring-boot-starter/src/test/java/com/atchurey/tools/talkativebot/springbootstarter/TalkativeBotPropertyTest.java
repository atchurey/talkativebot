package com.atchurey.tools.talkativebot.springbootstarter;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.configs.TalkativeBotProperties;
import com.atchurey.tools.talkativebot.springbootstarter.configs.TalkativeBotAutoConfiguration;

import com.atchurey.tools.talkativebot.springbootstarter.configs.properties.SpringBootTalkativeBotProperties;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {TalkativeBotAutoConfiguration.class})
public class TalkativeBotPropertyTest {

    @Autowired
    private TalkativeBotProperties talkativebotProperties;
    @Autowired
    private SpringBootTalkativeBotProperties springBootTalkativebotProperties;
    @Autowired
    private TalkativeBot talkativeBot;

    @Test
    public void contextLoads() {
        assertThat(talkativebotProperties.getHello()).isNotEmpty();
        assertThat(talkativebotProperties.getTopicBasePackage()).isNotEmpty();
        assertThat(talkativebotProperties.getChannels()).isNotNull();
        assertThat(talkativebotProperties.getChannels().getConsoleAddress()).isNotNull();

        SoftAssertions.assertSoftly(softly -> {
            if (talkativebotProperties.getChannels().isConsoleEnabled()) {
                softly.assertThat(talkativebotProperties.getChannels().isEnabled())
                        .as("If console channel is enabled then channels should be enabled too.")
                        .isTrue();
            }
        });

        assertThat(talkativebotProperties.getChannels()).satisfies(channels -> {
            if (channels.isConsoleEnabled()) {
                assertThat(talkativebotProperties.getChannels().getConsoleAddress())
                        .as("If console channel is enabled then console channel address must be set.")
                        .isNotNull();
            }
        });

        assertThat(talkativebotProperties.getPendingInteraction()).isNotNull();
        assertThat(talkativebotProperties.getPendingInteraction().getTtl()).isNotNull();
        assertThat(talkativebotProperties.getPendingInteraction().getTtl()).isPositive();
        assertThat(talkativebotProperties.getPendingInteraction().getStore()).isNotNull();
    }

    @Test
    public void propertiesAreTransferredToTalkativeBot() {
        assertThat(talkativebotProperties.getHello()).isEqualTo(springBootTalkativebotProperties.getHello());
        assertThat(talkativebotProperties.getTopicBasePackage()).isEqualTo(springBootTalkativebotProperties.getTopicBasePackage());

        //If A exists, B must exist.
        assertThat(talkativebotProperties.getChannels()).satisfies(channels -> {
            if (channels != null) {
                assertThat(springBootTalkativebotProperties.getChannels())
                        .as("Channels property not transferred properly.")
                        .isNotNull();

                assertThat(channels.isEnabled()).isEqualTo(springBootTalkativebotProperties.getChannels().isEnabled());
                assertThat(channels.isConsoleEnabled()).isEqualTo(springBootTalkativebotProperties.getChannels().isConsoleEnabled());
                assertThat(channels.getConsoleAddress()).isEqualTo(springBootTalkativebotProperties.getChannels().getConsoleAddress());
            }
        });

        assertThat(talkativebotProperties.getPendingInteraction()).satisfies(pi -> {
            if (pi != null) {
                assertThat(springBootTalkativebotProperties.getPendingInteraction())
                        .as("PendingInteraction property not transferred properly.")
                        .isNotNull();

                assertThat(pi.getTtl()).isEqualTo(springBootTalkativebotProperties.getPendingInteraction().getTtl());
                assertThat(pi.getStore()).isEqualTo(TalkativeBotProperties.PendingInteraction.StoreType.valueOf(springBootTalkativebotProperties.getPendingInteraction().getStore().name()));
            }
        });
    }

}