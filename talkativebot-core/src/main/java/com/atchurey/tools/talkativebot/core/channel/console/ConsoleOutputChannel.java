package com.atchurey.tools.talkativebot.core.channel.console;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.OutgoingMessage;
import com.atchurey.tools.talkativebot.core.channel.OutputChannel;
import com.atchurey.tools.talkativebot.core.questions.Option;

import java.util.concurrent.CompletableFuture;

public class ConsoleOutputChannel implements OutputChannel {

    @Override
    public String name() {
        return "console";
    }

    @Override
    public boolean supports(ConversationAddress address) {
        return "console".equalsIgnoreCase(address.getChannel());
    }

    @Override
    public CompletableFuture<Void> send(OutgoingMessage message) {
        if (message.isQuestion()) {
            System.out.println(message.getQuestion().getText());
            if (message.getQuestion().isChoice()) {
                for (Option option : message.getQuestion().getOptions()) {
                    System.out.printf("%d. %s%n", option.getValue(), option.getText());
                }
            }
        } else {
            System.out.println(message.getText());
        }

        return CompletableFuture.completedFuture(null);
    }
}
