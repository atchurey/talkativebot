package com.atchurey.tools.talkativebot.springbootstarter.configs;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializer;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeInitializerResolver;
import com.atchurey.tools.talkativebot.core.conversation.ConversationRuntimeRegistry;
import com.atchurey.tools.talkativebot.core.conversation.DefaultConversationRuntimeInitializerResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration(before = TalkativeBotAutoConfiguration.class)
@ConditionalOnClass(TalkativeBot.class)
public class TalkativeBotConversationRuntimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConversationRuntimeRegistry conversationRuntimeRegistry() {
        return new ConversationRuntimeRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationRuntimeInitializerResolver runtimeInitializerResolver(
            List<ConversationRuntimeInitializer> runtimeInitializers) {
        return new DefaultConversationRuntimeInitializerResolver(runtimeInitializers);    }

}
