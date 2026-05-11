package com.example.console;

import com.atchurey.tools.talkativebot.core.bot.TalkativeBot;
import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.example.console.conversations.SaleConversation;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ExampleConsoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleConsoleApplication.class, args);
	}

	@Component
	public static class SaleConversationRunner implements ApplicationRunner {

		private final TalkativeBot talkativeBot; // Constructor injection of the TalkativeBot
		public SaleConversationRunner(TalkativeBot talkativeBot) {
			this.talkativeBot = talkativeBot;
		}

		@Override
		public void run(ApplicationArguments args) {

			// To manually play a conversation, supply the conversation instance to the TalkativeBot.play() method.
			// As long as the property atchurey.tools.talkativebot.topic-base-package is properly configured, your topics are
			// automatically scanned and registered with the Conversation.
			ConversationAddress consoleAddress = talkativeBot.getBotConfigProperties()
					.getChannels().getConsoleAddress();
			SaleConversation saleConversation = new SaleConversation(talkativeBot, consoleAddress);
			talkativeBot.play(saleConversation);
		}

	}
}
